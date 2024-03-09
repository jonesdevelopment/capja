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

import org.jetbrains.annotations.NotNull;
import xyz.jonesdev.captcha.CaptchaImageGenerator;

// Mostly taken from
// https://github.com/logicsquad/nanocaptcha/blob/develop/src/main/java/net/logicsquad/nanocaptcha/image/filter/FishEyeImageFilter.java
public final class FishEyeImageFilter implements CaptchaFilter {

  @Override
  public void apply(final @NotNull CaptchaImageGenerator generator) {
    final int height = generator.getBufferedImage().getHeight();
    final int width = generator.getBufferedImage().getWidth();

    final int[] src = new int[width * height];
    generator.getBufferedImage().getRGB(0, 0, width, height, src, 0, width);

    final double randomDistance = (int) (width / 6 + ((width / 4 - width / 6) + 1) * Math.random());

    final int halfWidth = generator.getBufferedImage().getWidth() / 2;
    final int halfHeight = generator.getBufferedImage().getHeight() / 2;

    for (int x = 0; x < generator.getBufferedImage().getWidth(); x++) {
      for (int y = 0; y < generator.getBufferedImage().getHeight(); y++) {
        final int relX = x - halfWidth;
        final int relY = y - halfHeight;

        final double distance = Math.sqrt(relX * relX + relY * relY);
        if (distance >= randomDistance + halfWidth / 5f) continue;

        final double factor = Math.min(fishEye(distance / randomDistance) * randomDistance / distance, 1);

        final int newX = halfWidth + (int) ((factor) * (x - halfWidth));
        final int newY = halfHeight + (int) ((factor) * (y - halfHeight));

        generator.getBufferedImage().setRGB(x, y, src[newX * height + newY]);
      }
    }
  }

  private double fishEye(final double src) {
    if (src < 0) {
      return 0;
    }

    if (src > 1) {
      return src;
    }

    return -0.75D * src * src * src + 1.5D * src * src + 0.25D * src;
  }
}
