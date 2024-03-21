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

package xyz.jonesdev.capja;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.jonesdev.capja.config.CaptchaConfiguration;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

@Getter
public final class CaptchaGenerator {
  private final CaptchaConfiguration config;
  private final char[] rawCaptchaAnswer;
  private final CaptchaImageGenerator captchaImageGenerator;
  private @Nullable CaptchaHolder cachedCaptchaHolder;

  private static final Random RANDOM = new Random();
  private static final int[] FONT_TYPES = {Font.PLAIN, Font.BOLD};
  private static final String[] FONT_NAMES = {Font.DIALOG, Font.DIALOG_INPUT, Font.SANS_SERIF, Font.MONOSPACED};
  private static final char[] DEFAULT_DICTIONARY = {'0', '1', '2', '3', '5', '6', '9'};

  private static final CaptchaConfiguration DEFAULT_CONFIG = new CaptchaConfiguration(
    128, 128, DEFAULT_DICTIONARY, 5,
    true, true, true, true, true,
    0.3f, 2f, FONT_TYPES, FONT_NAMES);

  public CaptchaGenerator() {
    this(DEFAULT_CONFIG);
  }

  public CaptchaGenerator(final CaptchaConfiguration config) {
    this.config = config;
    this.rawCaptchaAnswer = generateRandomAnswer();
    this.captchaImageGenerator = new CaptchaImageGenerator(config);
  }

  /**
   * @return {@link CaptchaHolder} instance for this generation
   */
  public synchronized CaptchaHolder generate() {
    if (cachedCaptchaHolder == null) {
      final BufferedImage image = captchaImageGenerator.createImage(rawCaptchaAnswer);
      cachedCaptchaHolder = new CaptchaHolder(new String(rawCaptchaAnswer), image);
    }
    return cachedCaptchaHolder;
  }

  /**
   * @return Randomly generated CAPTCHA answer as a char[]
   */
  private char @NotNull [] generateRandomAnswer() {
    final char[] answer = new char[config.getAnswerLength()];
    for (int i = 0; i < answer.length; i++) {
      answer[i] = config.getDictionary()[RANDOM.nextInt(config.getDictionary().length)];
    }
    return answer;
  }
}
