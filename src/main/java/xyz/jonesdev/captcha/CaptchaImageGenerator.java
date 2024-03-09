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

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;
import xyz.jonesdev.captcha.filters.*;
import xyz.jonesdev.captcha.palette.MCColorPaletteConverter;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static java.awt.image.BufferedImage.TYPE_INT_RGB;

@Getter
public final class CaptchaImageGenerator {
  private static final Random RANDOM = new Random();
  private static final int[] FONT_TYPES = {Font.PLAIN, Font.BOLD};
  private static final String[] FONT_NAMES = {Font.DIALOG_INPUT, Font.SANS_SERIF, Font.MONOSPACED};

  private final List<CaptchaFilter> filters = new ArrayList<>();
  @Setter
  private BufferedImage bufferedImage;
  private Graphics2D graphics;

  public CaptchaImageGenerator(final @NotNull CaptchaProperties properties) {
    if (properties.getConfig().isFishEye()) {
      filters.add(new FishEyeImageFilter());
    }
    if (properties.getConfig().isShear()) {
      filters.add(new ShearImageFilter());
    }
    if (properties.getConfig().isElements()) {
      filters.add(new ElementsImageFilter(5));
    }
    if (properties.getConfig().isBlur()) {
      filters.add(new BlurImageFilter());
    }
  }

  public byte[] createBuffer(final @NotNull CaptchaProperties properties) throws IOException {
    // Create an RGB buffered image for the CAPTCHA
    bufferedImage = new BufferedImage(
      properties.getConfig().getImageWidth(), properties.getConfig().getImageHeight(), TYPE_INT_RGB);
    //bufferedImage = ImageIO.read(getClass().getResourceAsStream("/textures/background.png"));
    // Get the 2D graphics object for the image
    graphics = (Graphics2D) bufferedImage.getGraphics();

    // Draw background
    graphics.setBackground(Color.WHITE);
    graphics.fillRect(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight());
    // Change some rendering hints for quality and performance
    graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
    graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    // Draw characters
    drawCharacters(bufferedImage, graphics, properties.getConfig(), properties.getAnswerCharacters());

    // Apply filters
    for (final CaptchaFilter filter : filters) {
      filter.apply(this);
    }
    ImageIO.write(bufferedImage, "png", new File("1.png"));
    return MCColorPaletteConverter.toMapBytes(bufferedImage);
  }

  private void drawCharacters(final @NotNull BufferedImage bufferedImage,
                              final @NotNull Graphics2D graphics,
                              final @NotNull CaptchaConfiguration config,
                              final char[] chars) {
    // Create font render context
    final FontRenderContext ctx = graphics.getFontRenderContext();
    final Font defaultFont = new Font(Font.DIALOG, Font.PLAIN,
      46 + RANDOM.nextInt(6) - (config.getAnswerLength() * 2));

    // Calculate string width
    final double stringWidth = defaultFont.getStringBounds(chars, 0, chars.length, ctx).getWidth() * 0.9;
    // Calculate character positions
    final double beginX = (bufferedImage.getWidth() - stringWidth) * 0.5;
    final double beginY = (bufferedImage.getHeight() + defaultFont.getSize() * 0.5) * 0.5;
    double currentX = beginX;

    // Draw each character one by one
    for (final char character : chars) {
      graphics.setColor(getRandomColor(10, 80));

      // Create a font with the chosen font name
      final Font font = new Font(
        FONT_NAMES[RANDOM.nextInt(FONT_NAMES.length)],
        FONT_TYPES[RANDOM.nextInt(FONT_TYPES.length)], defaultFont.getSize());
      graphics.setFont(font);

      // Create a glyph vector for the character
      final GlyphVector glyphVector = font.createGlyphVector(ctx, String.valueOf(character));
      final Shape outlineShape = glyphVector.getOutline();
      // Apply a transformation to the glyph vector using AffineTransform
      final AffineTransform transformation = AffineTransform.getTranslateInstance(currentX, beginY);
      if (config.isRotate()) {
        // Apply a distortion effect
        transformation.shear(
          Math.sin(currentX * (Math.random() * 0.1) * 3) * Math.PI / 15,
          Math.sin(beginY * (Math.random() * 0.1) * 3) * Math.PI / 15);
        // Apply a rotation effect
        transformation.rotate(Math.sin(currentX) * Math.PI / 15);
      }
      final Shape shape = transformation.createTransformedShape(outlineShape);
      graphics.fill(shape);
      // Update next X position
      final double characterSpacing = 3;
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
