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

import java.awt.*;
import java.awt.image.BufferedImage;

@UtilityClass
public class MCColorPaletteConverter {
  private static final Color[] BASE_PALETTE = {
    new Color(0, 0, 0),
    new Color(127, 178, 56),
    new Color(247, 233, 163),
    new Color(199, 199, 199),
    new Color(255, 0, 0),
    new Color(160, 160, 160),
    new Color(167, 167, 167),
    new Color(0, 124, 0),
    new Color(255, 255, 255),
    new Color(164, 168, 184),
    new Color(151, 109, 77),
    new Color(112, 112, 112),
    new Color(64, 64, 255),
    new Color(143, 119, 72),
    new Color(255, 252, 245),
    new Color(216, 127, 51),
    new Color(178, 76, 216),
    new Color(102, 153, 216),
    new Color(229, 229, 51),
    new Color(127, 204, 25),
    new Color(242, 127, 165),
    new Color(76, 76, 76),
    new Color(153, 153, 153),
    new Color(76, 127, 153),
    new Color(127, 63, 178),
    new Color(51, 76, 178),
    new Color(102, 76, 51),
    new Color(102, 127, 51),
    new Color(153, 51, 51),
    new Color(25, 25, 25),
    new Color(250, 238, 77),
    new Color(92, 219, 213),
    new Color(74, 128, 255),
    new Color(0, 217, 58),
    new Color(129, 86, 49),
    new Color(112, 2, 0),
    new Color(209, 177, 161),
    new Color(159, 82, 36),
    new Color(149, 87, 108),
    new Color(112, 108, 138),
    new Color(186, 133, 36),
    new Color(103, 117, 53),
    new Color(160, 77, 78),
    new Color(57, 41, 35),
    new Color(135, 107, 98),
    new Color(87, 92, 92),
    new Color(122, 73, 88),
    new Color(76, 62, 92),
    new Color(76, 50, 35),
    new Color(76, 82, 42),
    new Color(142, 60, 46),
    new Color(37, 22, 16),
    new Color(189, 48, 49),
    new Color(148, 63, 97),
    new Color(92, 25, 29),
    new Color(22, 126, 134),
    new Color(58, 142, 140),
    new Color(86, 44, 62),
    new Color(20, 180, 133),
    new Color(100, 100, 100),
    new Color(216, 175, 147),
    new Color(127, 167, 150),
  };

  public byte @NotNull [] toMapBytes(final @NotNull BufferedImage bufferedImage) {
    final int[] result = bufferedImage.getRGB(
      0, 0, bufferedImage.getWidth(), bufferedImage.getHeight(),
      null, 0, bufferedImage.getWidth());
    final byte[] buffer = new byte[result.length];
    for (int i = 0; i < buffer.length; ++i) {
      buffer[i] = matchPaletteColor(new Color(result[i]));
    }
    return buffer;
  }

  private byte matchPaletteColor(final @NotNull Color color) {
    // Make sure to avoid trying to match transparent colors
    if (color.getAlpha() < 128) return 0;
    // Map all colors to bytes
    return nearestColor(color);
  }

  private byte nearestColor(final Color color) {
    double min = -1, distance;
    byte nearest = 0;
    byte index = 0;
    for (final Color c : BASE_PALETTE) {
      ++index;
      distance = distance(color, c);
      if (min == -1 || distance < min) {
        min = distance;
        nearest = index;
      }
    }
    return nearest;
  }

  private double distance(final @NotNull Color color,
                          final @NotNull Color color1) {
    final int[] i1 = new int[] {color.getRed(), color.getGreen(), color.getBlue()};
    final int[] i2 = new int[] {color1.getRed(), color1.getGreen(), color1.getBlue()};
    double distance = 0;
    for (int i = 0; i < 3; i++) {
      distance += Math.pow(i1[i] - i2[i], 2);
    }
    return distance;
  }
}
