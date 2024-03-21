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

package xyz.jonesdev.captcha.test;

import lombok.experimental.UtilityClass;
import xyz.jonesdev.captcha.CaptchaGenerator;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

@UtilityClass
public class Main {
  public void main(final String... args) throws IOException {
    ImageIO.write(new CaptchaGenerator().generate(), "png", new File("1.png"));
  }
}
