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

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.util.ArrayList;
import java.util.List;

import static java.awt.image.BufferedImage.TYPE_INT_RGB;

@Getter
public final class CaptchaImageGenerator {
  private final List<BufferedImageOp> randomFilters = new ArrayList<>(9);
  private final CaptchaConfiguration config;
  @Setter
  private BufferedImage bufferedImage;
  private Graphics2D graphics;

  public CaptchaImageGenerator(final @NotNull CaptchaConfiguration config) {
    this.config = config;

    // Prepare filters
    if (config.isScratches()) {
      final CustomScratchFilter scratchFilter = new CustomScratchFilter(
        5 + config.getRandom().nextInt(6));
      randomFilters.add(scratchFilter);
    }
    randomFilters.add(new UnsharpFilter());
    randomFilters.add(new MinimumFilter());
    randomFilters.add(new MaximumFilter());
    randomFilters.add(new SaturationFilter(config.getSaturation()));
    if (config.isFlare()) {
      final FlareFilter flareFilter = new FlareFilter();
      flareFilter.setRadius(config.getImageWidth() / 3f);
      flareFilter.setBaseAmount(0.7f);
      randomFilters.add(flareFilter);
    }
    if (config.isRipple()) {
      final RippleFilter rippleFilter = new RippleFilter();
      rippleFilter.setXAmplitude(5 + (int) (0.5 - config.getRandom().nextDouble() * 3));
      rippleFilter.setYAmplitude(10 + (int) (0.5 - config.getRandom().nextDouble() * 6));
      randomFilters.add(rippleFilter);
    }
    if (config.isSmear()) {
      final SmearFilter smearFilter = new SmearFilter();
      smearFilter.setDensity(0.075f * config.getRandom().nextFloat());
      randomFilters.add(smearFilter);
    }
    if (config.isPinch()) {
      final PinchFilter pinchFilter = new PinchFilter();
      pinchFilter.setAmount((float) (0.5 - config.getRandom().nextDouble()) * 0.1f);
      randomFilters.add(pinchFilter);
    }
  }

  @SuppressWarnings("unused")
  public @NotNull BufferedImage createImage(final char[] answer) {
    // Create an RGB buffered image for the CAPTCHA
    BufferedImage bufferedImage = new BufferedImage(config.getImageWidth(), config.getImageHeight(), TYPE_INT_RGB);
    bufferedImage = new CausticsFilter().filter(bufferedImage, null);
    // Get the 2D graphics object for the image
    graphics = (Graphics2D) bufferedImage.getGraphics();

    // Change some rendering hints for quality and performance
    graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
    graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    // Draw characters
    drawCharacters(bufferedImage, graphics, config, answer);

    // Apply filters
    for (final BufferedImageOp filter : randomFilters) {
      bufferedImage = filter.filter(bufferedImage, null);
    }
    return bufferedImage;
  }

  private void drawCharacters(final @NotNull BufferedImage bufferedImage,
                              final @NotNull Graphics2D graphics,
                              final @NotNull CaptchaConfiguration config,
                              final char[] chars) {
    // Create font render context
    final FontRenderContext ctx = graphics.getFontRenderContext();
    final Font defaultFont = new Font(Font.DIALOG, Font.PLAIN,
      47 + config.getRandom().nextInt(6) - (config.getAnswerLength() * 2));

    // Calculate string width
    final double stringWidth = defaultFont.getStringBounds(chars, 0, chars.length, ctx).getWidth() * 0.9;
    // Calculate character positions
    final double beginX = (bufferedImage.getWidth() - stringWidth) * 0.5;
    final double beginY = (bufferedImage.getHeight() + defaultFont.getSize() * 0.5) * 0.5;
    double currentX = beginX;

    // Draw each character one by one
    for (final char character : chars) {
      graphics.setColor(getRandomColor(50, 90));

      // Make sure to randomize the font name & type
      final int randomFontType = config.getFontTypes()[config.getRandom().nextInt(config.getFontTypes().length)];
      final String randomFontName = config.getFontNames()[config.getRandom().nextInt(config.getFontNames().length)];
      // Create a font with the chosen font name
      @SuppressWarnings("all")
      final Font font = new Font(
        // 1 is easier to read for a human when Monospaced is used
        character == 1 ? Font.MONOSPACED : randomFontName, randomFontType, defaultFont.getSize());
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
    final int r = Math.min(min + config.getRandom().nextInt(bound), 255);
    final int g = Math.min(min + config.getRandom().nextInt(bound), 255);
    final int b = Math.min(min + config.getRandom().nextInt(bound), 255);
    return new Color(r, g, b);
  }
}
