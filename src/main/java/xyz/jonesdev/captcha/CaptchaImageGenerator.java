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

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;
import xyz.jonesdev.captcha.palette.MCColorPaletteConverter;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.IOException;
import java.util.Random;

import static java.awt.image.BufferedImage.TYPE_INT_RGB;

@UtilityClass
public class CaptchaImageGenerator {
  private final double SPACING = 3.5;
  private final Random RANDOM = new Random();
  private final int[] FONT_TYPES = {Font.PLAIN, Font.BOLD, Font.ITALIC, Font.ITALIC | Font.BOLD};

  private final BufferedImageOp BLUR_CONVOLVE_OP;

  static {
    {
      // http://www.java2s.com/Tutorial/Java/0261__2D-Graphics/BlurourimageBlurmeansanunfocusedimage.htm
      final float[] blurKernel = {1 / 9f, 1 / 9f, 1 / 9f, 1 / 9f, 1 / 9f, 1 / 9f, 1 / 9f, 1 / 9f, 1 / 9f};
      // Create the convolution kernel
      final Kernel kernel = new Kernel(3, 3, blurKernel);
      // Create the ConvolveOp object with the kernel
      BLUR_CONVOLVE_OP = new ConvolveOp(kernel);
    }
  }

  public byte[] createBuffer(final @NotNull CaptchaProperties properties) throws IOException {
    // Create an RGB buffered image for the CAPTCHA
    BufferedImage bufferedImage = new BufferedImage(
      properties.getConfig().getImageWidth(), properties.getConfig().getImageHeight(), TYPE_INT_RGB);
    // Get the 2D graphics object for the image
    final Graphics2D graphics = (Graphics2D) bufferedImage.getGraphics();

    // Draw background
    graphics.setBackground(Color.WHITE);
    graphics.fillRect(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight());
    // Change some rendering hints for quality and performance
    graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
    graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    // Draw random elements on background
    drawRandomElements(bufferedImage, graphics, properties.getConfig().getRandomElementsAmount());
    // Draw characters
    drawCharacters(bufferedImage, graphics, properties.getConfig(), properties.getAnswerCharacters());

    // Apply blur effect
    if (properties.getConfig().isBlur()) {
      bufferedImage = BLUR_CONVOLVE_OP.filter(bufferedImage, null);
    }
    return MCColorPaletteConverter.toMapBytes(bufferedImage);
  }

  private void drawRandomElements(final @NotNull BufferedImage bufferedImage,
                                  final @NotNull Graphics2D graphics,
                                  final int amount) {
    final int halfWidth = bufferedImage.getWidth() / 2;
    final int halfHeight = bufferedImage.getHeight() / 2;

    for (int i = 0; i < amount; i++) {
      final int startX = RANDOM.nextInt(halfWidth);
      final int startY = RANDOM.nextInt(halfHeight);
      final int endX = halfWidth + RANDOM.nextInt(halfWidth);
      final int endY = halfHeight + RANDOM.nextInt(halfHeight);

      final int r = 80 + RANDOM.nextInt(80);
      final int g = 80 + RANDOM.nextInt(80);
      final int b = 80 + RANDOM.nextInt(80);
      graphics.setColor(new Color(r, g, b));

      // Select a random element
      switch (RANDOM.nextInt(4)) {
        default:
        case 0:
          graphics.drawLine(startX, startY, endX, endY);
          break;
        case 1:
          graphics.drawOval(startX, startY, endX, endY);
          break;
        case 2:
          graphics.fillRect(startX, startY, 1, 1);
          break;
      }
    }
  }

  private void drawCharacters(final @NotNull BufferedImage bufferedImage,
                              final @NotNull Graphics2D graphics,
                              final @NotNull CaptchaConfiguration config,
                              final char[] chars) {
    // Set font for the text
    @SuppressWarnings("all")
    final Font font = new Font(
      Font.MONOSPACED, FONT_TYPES[RANDOM.nextInt(FONT_TYPES.length)],
      45 + RANDOM.nextInt(6) - (config.getAnswerLength() * 2));
    graphics.setFont(font);

    // Create font render context
    final FontRenderContext ctx = graphics.getFontRenderContext();

    // Calculate string width
    final double stringWidth = font.getStringBounds(chars, 0, chars.length, ctx).getWidth();
    // Calculate character positions
    final double beginX = (bufferedImage.getWidth() - stringWidth) * 0.5;
    final double beginY = (bufferedImage.getHeight() + font.getSize() * 0.5) * 0.5;
    double currentX = beginX;
    double currentY = beginY;

    // Draw each character one by one
    for (final char character : chars) {
      final int r = RANDOM.nextInt(90);
      final int g = RANDOM.nextInt(90);
      final int b = RANDOM.nextInt(90);
      graphics.setColor(new Color(r, g, b));

      // Create a glyph vector for the character
      final GlyphVector glyphVector = font.createGlyphVector(ctx, String.valueOf(character));
      final Shape outlineShape = glyphVector.getOutline();
      // Apply a transformation to the glyph vector using AffineTransform
      final AffineTransform transformation = AffineTransform.getTranslateInstance(currentX, currentY);
      if (config.isRotate()) {
        // Apply a ripple/rotation effect
        transformation.rotate(Math.sin(currentX / 2) * Math.PI / (14 + RANDOM.nextInt(6)));
      }
      if (config.isScale()) {
        // Apply a ripple/rotation effect
        transformation.scale(1 + Math.sin(currentX) / 6, 1 + Math.sin(currentY) / 6);
      }
      // Draw the character
      final Shape shape = transformation.createTransformedShape(outlineShape);
      graphics.fill(shape);
      // Update next X position
      currentX += glyphVector.getVisualBounds().getWidth() + SPACING;
      if (config.isRandomizePosition()) {
        // Randomize next position
        currentX += (0.5 - RANDOM.nextDouble()) * SPACING;
        currentY += (0.5 - RANDOM.nextDouble()) * SPACING;
      }
    }
  }
}
