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

@RequiredArgsConstructor
public final class ElementsImageFilter implements CaptchaFilter {
  private final int lines;

  @Override
  public void apply(final @NotNull CaptchaImageGenerator generator) {
    final int halfWidth = generator.getBufferedImage().getWidth() / 2;
    final int halfHeight = generator.getBufferedImage().getHeight() / 2;

    for (int i = 0; i < lines; i++) {
      final int startX = RANDOM.nextInt(halfWidth);
      final int startY = RANDOM.nextInt(halfHeight);
      final int endX = halfWidth + RANDOM.nextInt(halfWidth);
      final int endY = halfHeight + RANDOM.nextInt(halfHeight);

      generator.getGraphics().setColor(generator.getRandomColor(70, 90));
      if (RANDOM.nextInt(10) < 7) {
        generator.getGraphics().drawLine(startX, startY, endX, endY);
      } else {
        generator.getGraphics().drawOval(startX, startY, endX, endY);
      }
    }
  }
}
