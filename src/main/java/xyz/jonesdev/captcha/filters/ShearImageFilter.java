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

import java.awt.*;

// Mostly taken from
// https://github.com/logicsquad/nanocaptcha/blob/develop/src/main/java/net/logicsquad/nanocaptcha/image/filter/ShearImageFilter.java
@RequiredArgsConstructor
public final class ShearImageFilter implements CaptchaFilter {
  private final int div = 20;
  private final int phase = 100;

  @Override
  public void apply(final @NotNull CaptchaImageGenerator generator) {
    final int height = generator.getBufferedImage().getHeight();
    final int width = generator.getBufferedImage().getWidth();

    shearX(generator.getGraphics(), width, height);
    shearY(generator.getGraphics(), width, height);
  }

  private void shearX(final Graphics2D graphics, final int width, final int height) {
    final int period = RANDOM.nextInt(10) + 5;
    final int phase = RANDOM.nextInt(5) + 2;

    for (int i = 0; i < height; i++) {
      double d = (period >> 1) * Math.sin((double) i / (double) period + (Math.PI * 2 * phase) / div);
      graphics.copyArea(0, i, width, 1, (int) d, 0);
      graphics.setColor(Color.WHITE);
      graphics.drawLine((int) d, i, 0, i);
      graphics.drawLine((int) d + width, i, width, i);
    }
  }

  private void shearY(final Graphics2D graphics, final int width, final int height) {
    int period = RANDOM.nextInt(30) + 10;

    for (int i = 0; i < width; i++) {
      double d = (period >> 1) * Math.sin((float) i / period + (Math.PI * 2 * phase) / div);
      graphics.copyArea(i, 0, 1, height, 0, (int) d);
      graphics.setColor(Color.WHITE);
      graphics.drawLine(i, (int) d, i, 0);
      graphics.drawLine(i, (int) d + height, i, height);
    }
  }
}
