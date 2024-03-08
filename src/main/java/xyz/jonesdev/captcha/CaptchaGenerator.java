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

import java.io.IOException;
import java.util.Random;

@Getter
public final class CaptchaGenerator {
  private final CachedMapCaptcha[] cached;
  private final CaptchaConfiguration config;
  private int loadedCaptchas;

  private static final Random RANDOM = new Random();
  private static final char[] NUMBERS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};

  private static final CaptchaConfiguration DEFAULT_CONFIG = new CaptchaConfiguration(
    128, 128, NUMBERS, 5,
    true, true, true, true, true);

  public CaptchaGenerator(final int precomputeAmount) {
    this(precomputeAmount, DEFAULT_CONFIG);
  }

  public CaptchaGenerator(final int precomputeAmount,
                          final CaptchaConfiguration config) {
    this.cached = new CachedMapCaptcha[precomputeAmount];
    this.config = config;
    prepareAll();
  }

  private void prepareAll() {
    for (int i = 0; i < cached.length; i++) {
      // Generate random CAPTCHA answers and prepare the CAPTCHA
      final char[] rawCharactersAnswer = generateRandomAnswer();
      final CaptchaProperties properties = new CaptchaProperties(
        new String(rawCharactersAnswer), rawCharactersAnswer, config);
      cached[i] = prepare(properties);
      loadedCaptchas++;
    }
  }

  private @NotNull CachedMapCaptcha prepare(final @NotNull CaptchaProperties properties) {
    // Create an image for the CAPTCHA
    byte[] buffer;
    try {
      final CaptchaImageGenerator imageGenerator = new CaptchaImageGenerator(properties);
      buffer = imageGenerator.createBuffer(properties);
    } catch (IOException exception) {
      throw new IllegalStateException("Could not create buffer", exception);
    }
    // Create a new cached CAPTCHA and return the object
    return new CachedMapCaptcha(properties.getAnswer(), buffer);
  }

  private char @NotNull [] generateRandomAnswer() {
    final char[] answer = new char[config.getAnswerLength()];
    for (int i = 0; i < answer.length; i++) {
      answer[i] = config.getDictionary()[RANDOM.nextInt(config.getDictionary().length)];
    }
    return answer;
  }

  @SuppressWarnings("unused")
  public CachedMapCaptcha getRandomCaptcha() {
    return cached[RANDOM.nextInt(loadedCaptchas)];
  }
}
