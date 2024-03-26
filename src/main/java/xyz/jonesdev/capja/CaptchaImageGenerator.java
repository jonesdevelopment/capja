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

package xyz.jonesdev.capja;

import com.jhlabs.image.*;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import xyz.jonesdev.capja.config.CaptchaConfiguration;
import xyz.jonesdev.capja.filters.CustomScratchFilter;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.awt.image.BufferedImage.TYPE_INT_ARGB;
import static java.awt.image.BufferedImage.TYPE_INT_RGB;

@Getter
public final class CaptchaImageGenerator {
  private final List<BufferedImageOp> textFilters = new ArrayList<>(9);
  private final List<BufferedImageOp> backgroundFilters = new ArrayList<>(9);
  private final CaptchaConfiguration config;

  public CaptchaImageGenerator(final @NotNull CaptchaConfiguration config) {
    this.config = config;

    // Add scratches (lines) to the background
    if (config.isScratches()) {
      final CustomScratchFilter scratchFilter = new CustomScratchFilter(
        5 + config.getRandom().nextInt(6));
      textFilters.add(scratchFilter);
    }
    // Apply random noise to the background
    {
      textFilters.add(new UnsharpFilter());
      textFilters.add(new SaturationFilter(config.getSaturation()));
    }
    // Add flare effect to the background
    if (config.isFlare()) {
      final FlareFilter flareFilter = new FlareFilter();
      flareFilter.setRadius(config.getImageWidth() / 3f + config.getRandom().nextFloat());
      flareFilter.setBaseAmount(0.3f);
      backgroundFilters.add(flareFilter);
    }
    // Add ripple (wave) effect using sine
    if (config.isRipple()) {
      final RippleFilter rippleFilter = new RippleFilter();
      rippleFilter.setXAmplitude(5 + (0.5f - config.getRandom().nextFloat()) * 3);
      rippleFilter.setYAmplitude(10 + (0.5f - config.getRandom().nextFloat()) * 6);
      textFilters.add(rippleFilter);
    }
    // Apply triangular distortion (X only)
    {
      final RippleFilter distortionFilter = new RippleFilter();
      distortionFilter.setXAmplitude(config.getDistortion());
      distortionFilter.setWaveType(RippleFilter.TRIANGLE);
      textFilters.add(distortionFilter);
    }
    // Add smear (distorted pixels)
    if (config.isSmear()) {
      final SmearFilter smearFilter = new SmearFilter();
      smearFilter.setDensity(0.075f * config.getRandom().nextFloat());
      textFilters.add(smearFilter);
    }
    // Add pinch (smoothen)
    if (config.isPinch()) {
      final PinchFilter pinchFilter = new PinchFilter();
      pinchFilter.setAmount((0.5f - config.getRandom().nextFloat()) * 0.1f);
      textFilters.add(pinchFilter);
    }
  }

  @SuppressWarnings("unused")
  public BufferedImage createImage(final char[] answer) {
    BufferedImage background = createBufferedImage();

    final BufferedImage characters = createCharacters(background, answer);

    final Graphics2D graphics = background.createGraphics();
    graphics.drawImage(characters, 0, 0, config.getImageWidth(), config.getImageHeight(), null);
    graphics.dispose();

    // Modify saturation
    background = new SaturationFilter(config.getSaturation()).filter(background, null);
    return background;
  }

  private BufferedImage createCharacters(final BufferedImage background, final char[] answer) {
    // Create a buffered image for the CAPTCHA generation
    BufferedImage result = new BufferedImage(config.getImageWidth(), config.getImageHeight(), TYPE_INT_ARGB);
    // Get the 2D graphics object for the image
    final Graphics2D graphics = result.createGraphics();

    // Change some rendering hints for anti aliasing
    graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    // Draw characters
    drawCharacters(result, background, graphics, config, answer);
    // Make sure to dispose the graphics after using it
    graphics.dispose();

    // Apply filters
    for (final BufferedImageOp filter : textFilters) {
      result = filter.filter(result, null);
    }
    return result;
  }

  private BufferedImage createBufferedImage() {
    final int width = config.getImageWidth();
    final int height = config.getImageHeight();
    BufferedImage bufferedImage;

    if (config.getBackgroundImage() != null) {
      try {
        // Load background from external image
        bufferedImage = ImageIO.read(config.getBackgroundImage());

        // Resize the image if it has an incorrect width or height
        if (bufferedImage.getWidth() != width || bufferedImage.getHeight() != height) {
          final Graphics2D graphics = bufferedImage.createGraphics();
          bufferedImage = new BufferedImage(width, height, TYPE_INT_RGB);

          graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
          graphics.drawImage(bufferedImage, 0, 0, width, height, null);
          graphics.dispose();
        }
        return bufferedImage;
      } catch (IOException exception) {
        // Continue loading default background after warning the user
      }
    }

    // Create an RGB buffered image
    bufferedImage = new BufferedImage(width, height, TYPE_INT_RGB);
    bufferedImage = new CausticsFilter().filter(bufferedImage, null);

    // Apply background filters
    for (final BufferedImageOp filter : backgroundFilters) {
      bufferedImage = filter.filter(bufferedImage, null);
    }
    return bufferedImage;
  }

  private void drawCharacters(final @NotNull BufferedImage bufferedImage,
                              final @NotNull BufferedImage background,
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
      // Interpolate between background colors to make sure that the color we
      // end up using for the characters is actually not too bright nor too dark
      final int argb = background.getRGB((int) currentX + 5, (int) beginY + 5);
      graphics.setColor(getRandomInversedColor(argb));

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

  private @NotNull Color getRandomInversedColor(final int argb) {
    final Color darkOrNot = new Color(~argb);
    final int r = Math.max(Math.min(darkOrNot.getRed() + (config.getRandom().nextInt(75) - 25), 255), 0);
    final int g = Math.max(Math.min(darkOrNot.getGreen() + (config.getRandom().nextInt(75) - 25), 255), 0);
    final int b = Math.max(Math.min(darkOrNot.getBlue() + (config.getRandom().nextInt(75) - 25), 255), 0);
    return new Color(r, g, b, 255);
  }
}
