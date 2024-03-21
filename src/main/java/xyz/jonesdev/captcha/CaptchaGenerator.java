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

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.jonesdev.captcha.config.CaptchaConfiguration;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

@Getter
public final class CaptchaGenerator {
  private final CaptchaConfiguration config;
  private final char[] rawCaptchaAnswer;
  private final CaptchaProperties properties;
  private final CaptchaImageGenerator captchaImageGenerator;
  private @Nullable BufferedImage cachedCaptchaImage;

  private static final char[] DEFAULT_DICTIONARY = {'0', '1', '2', '3', '5', '6', '9'};
  private static final Random RANDOM = new Random();

  private static final CaptchaConfiguration DEFAULT_CONFIG = new CaptchaConfiguration(
    128, 128, DEFAULT_DICTIONARY, 5,
    true, true, true, true, true,
    RANDOM, new int[]{Font.PLAIN, Font.BOLD},
    new String[]{Font.DIALOG, Font.DIALOG_INPUT, Font.SANS_SERIF, Font.MONOSPACED});

  public CaptchaGenerator() {
    this(DEFAULT_CONFIG);
  }

  public CaptchaGenerator(final CaptchaConfiguration config) {
    this.config = config;
    rawCaptchaAnswer = generateRandomAnswer();
    this.properties = new CaptchaProperties(new String(rawCaptchaAnswer), rawCaptchaAnswer, config);
    this.captchaImageGenerator = new CaptchaImageGenerator(properties);
  }

  public BufferedImage generate() {
    if (cachedCaptchaImage == null) {
      cachedCaptchaImage = captchaImageGenerator.createImage();
    }
    return cachedCaptchaImage;
  }

  private char @NotNull [] generateRandomAnswer() {
    final char[] answer = new char[config.getAnswerLength()];
    for (int i = 0; i < answer.length; i++) {
      answer[i] = config.getDictionary()[RANDOM.nextInt(config.getDictionary().length)];
    }
    return answer;
  }
}
