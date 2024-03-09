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

import com.jhlabs.image.AbstractBufferedImageOp;
import lombok.RequiredArgsConstructor;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

@RequiredArgsConstructor
public final class CustomScratchFilter extends AbstractBufferedImageOp {
  private static final Random RANDOM = new Random();
  private final int amount;

  @Override
  public BufferedImage filter(final BufferedImage src, final BufferedImage dst) {
    final Graphics2D graphics = src.createGraphics();
    final float l = 0.5f * (float) src.getWidth();
    for (int i = 0; i < amount; ++i) {
      final float x = (float) src.getWidth() * RANDOM.nextFloat();
      final float y = (float) src.getHeight() * RANDOM.nextFloat();
      final float a = 6.2831855F * (RANDOM.nextFloat() - 0.5F);
      final float s = (float) Math.sin(a) * l;
      final float c = (float) Math.cos(a) * l;
      final float x1 = x - c;
      final float y1 = y - s;
      final float x2 = x + c;
      final float y2 = y + s;
      graphics.drawLine((int) x1, (int) y1, (int) x2, (int) y2);
    }
    graphics.dispose();
    return src;
  }
}
