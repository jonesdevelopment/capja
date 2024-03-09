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

import com.jhlabs.image.CausticsFilter;
import lombok.experimental.UtilityClass;

import java.awt.image.BufferedImage;

import static java.awt.image.BufferedImage.TYPE_INT_RGB;

@UtilityClass
public class BackgroundImageGenerator {
  public BufferedImage getRandomBackground(final CaptchaProperties properties) {
    BufferedImage bufferedImage = new BufferedImage(
      properties.getConfig().getImageWidth(), properties.getConfig().getImageHeight(), TYPE_INT_RGB);
    bufferedImage = new CausticsFilter().filter(bufferedImage, null);
    return bufferedImage;
  }
}
