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

package xyz.jonesdev.captcha.palette;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

import java.awt.image.BufferedImage;

@UtilityClass
public class MCColorPaletteConverter {
  public byte @NotNull [] toMapBytes(final @NotNull BufferedImage bufferedImage) {
    final int[] result = bufferedImage.getRGB(
      0, 0, bufferedImage.getWidth(), bufferedImage.getHeight(),
      null, 0, bufferedImage.getWidth());
    final byte[] buffer = new byte[result.length];
    for (int i = 0; i < buffer.length; ++i) {
      buffer[i] = matchPaletteColor(result[i]);
    }
    return buffer;
  }

  private byte matchPaletteColor(final int rgb) {
    // Make sure to avoid trying to match transparent colors
    if ((rgb & 0xFF000000) >>> 24 < 128) return 0;
    // Return the mapped color stored in the buffer
    System.out.println(rgb & 0xFFFFFF);
    return 0;
    //return COLOR_BUFFER[rgb & 0xFFFFFF];
  }
}
