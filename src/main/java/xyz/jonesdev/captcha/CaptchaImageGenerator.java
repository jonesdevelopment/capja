/*
 * Copyright (C) 2024 jones
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package xyz.jonesdev.captcha;

import com.jhlabs.image.*;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;
import xyz.jonesdev.captcha.config.CaptchaConfiguration;
import xyz.jonesdev.captcha.filters.CustomScratchFilter;
import xyz.jonesdev.captcha.palette.MCColorPaletteConverter;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.io.File;
import java.io.IOException;
import java.util.Random;

@Getter
public final class CaptchaImageGenerator {
  private static final Random RANDOM = new Random();

  private static final int[] FONT_TYPES = {Font.PLAIN, Font.BOLD};
  private static final String[] FONT_NAMES = {Font.DIALOG, Font.DIALOG_INPUT, Font.SANS_SERIF, Font.MONOSPACED};

  private static final BufferedImageOp[] FILTERS = {
    new CustomScratchFilter(8),
    //new OilFilter(), // blob-ify
    new UnsharpFilter(), // blur
    new MinimumFilter(), // crank image up
    new MaximumFilter(), // crank image down
    new SaturationFilter(0.3f), // change saturation
    //new NoiseFilter(), // random noise
  };

  private final BufferedImageOp[] randomFilters;
  private final CaptchaProperties properties;
  @Setter
  private BufferedImage bufferedImage;
  private Graphics2D graphics;

  public CaptchaImageGenerator(final @NotNull CaptchaProperties properties) {
    this.properties = properties;

    // Prepare filters
    final FlareFilter flareFilter = new FlareFilter();
    flareFilter.setRadius(properties.getConfig().getImageWidth() / 3f);
    flareFilter.setBaseAmount(0.7f);
    final RippleFilter rippleFilter = new RippleFilter();
    rippleFilter.setXAmplitude(5 + (int) (0.5 - Math.random() * 3));
    rippleFilter.setYAmplitude(10 + (int) (0.5 - Math.random() * 5));
    final SmearFilter smearFilter = new SmearFilter();
    smearFilter.setDensity(0.075f * (float) Math.random());
    final PinchFilter pinchFilter = new PinchFilter();
    pinchFilter.setAmount((float) (0.5 - Math.random()) * 0.1f);

    this.randomFilters = new BufferedImageOp[]{flareFilter, rippleFilter, smearFilter, pinchFilter};
  }

  public byte[] createImage() throws IOException {
    // Create an RGB buffered image for the CAPTCHA
    bufferedImage = BackgroundImageGenerator.getRandomBackground(properties);
    // Get the 2D graphics object for the image
    graphics = (Graphics2D) bufferedImage.getGraphics();

    // Change some rendering hints for quality and performance
    graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
    graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    // Draw characters
    drawCharacters(bufferedImage, graphics, properties.getConfig(), properties.getAnswerCharacters());

    // Apply filters
    applyFilters(FILTERS);
    applyFilters(randomFilters);

    // Save image (temporary)
    ImageIO.write(bufferedImage, "png", new File("1.png"));
    // Return converted color bytes
    return MCColorPaletteConverter.toMapBytes(bufferedImage);
  }

  private void applyFilters(final BufferedImageOp @NotNull [] filters) {
    for (final BufferedImageOp filter : filters) {
      bufferedImage = filter.filter(bufferedImage, null);
    }
  }

  private void drawCharacters(final @NotNull BufferedImage bufferedImage,
                              final @NotNull Graphics2D graphics,
                              final @NotNull CaptchaConfiguration config,
                              final char[] chars) {
    // Create font render context
    final FontRenderContext ctx = graphics.getFontRenderContext();
    final Font defaultFont = new Font(Font.DIALOG, Font.PLAIN,
      47 + RANDOM.nextInt(6) - (config.getAnswerLength() * 2));

    // Calculate string width
    final double stringWidth = defaultFont.getStringBounds(chars, 0, chars.length, ctx).getWidth() * 0.9;
    // Calculate character positions
    final double beginX = (bufferedImage.getWidth() - stringWidth) * 0.5;
    final double beginY = (bufferedImage.getHeight() + defaultFont.getSize() * 0.5) * 0.5;
    double currentX = beginX;

    // Draw each character one by one
    for (final char character : chars) {
      graphics.setColor(getRandomColor(30, 90));

      // Create a font with the chosen font name
      final Font font = new Font(
        // 1 is easier to read for a human when Monospaced is used
        character == 1 ? Font.MONOSPACED : FONT_NAMES[RANDOM.nextInt(FONT_NAMES.length)],
        FONT_TYPES[RANDOM.nextInt(FONT_TYPES.length)], defaultFont.getSize());
      graphics.setFont(font);

      // Create a glyph vector for the character
      final GlyphVector glyphVector = font.createGlyphVector(ctx, String.valueOf(character));
      // Apply a transformation to the glyph vector using AffineTransform
      final AffineTransform transformation = AffineTransform.getTranslateInstance(currentX, beginY);
      final Shape shape = transformation.createTransformedShape(glyphVector.getOutline());
      graphics.fill(shape);
      // Update next X position
      final double characterSpacing = 2.75;
      currentX += glyphVector.getVisualBounds().getWidth() + characterSpacing;
    }
  }

  public @NotNull Color getRandomColor(final @Range(from = 0, to = 255) int min,
                                       final @Range(from = 0, to = 255) int bound) {
    final int r = Math.min(min + RANDOM.nextInt(bound), 255);
    final int g = Math.min(min + RANDOM.nextInt(bound), 255);
    final int b = Math.min(min + RANDOM.nextInt(bound), 255);
    return new Color(r, g, b);
  }
}
