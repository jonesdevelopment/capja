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

package xyz.jonesdev.captcha.filters;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import xyz.jonesdev.captcha.CaptchaImageGenerator;

import java.awt.image.BufferedImageOp;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;

@RequiredArgsConstructor
public final class BlurImageFilter implements CaptchaFilter {
  // Convolve filter
  private static final BufferedImageOp BLUR_CONVOLVE_OP;

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

  @Override
  public void apply(final @NotNull CaptchaImageGenerator generator) {
    generator.setBufferedImage(BLUR_CONVOLVE_OP.filter(generator.getBufferedImage(), null));
  }
}
