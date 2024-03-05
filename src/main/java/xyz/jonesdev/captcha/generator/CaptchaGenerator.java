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

package xyz.jonesdev.captcha.generator;

import com.google.gson.Gson;
import lombok.Getter;
import xyz.jonesdev.captcha.CachedCaptcha;
import xyz.jonesdev.captcha.parser.CaptchaProperties;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.util.Enumeration;
import java.util.Random;

@Getter
public final class CaptchaGenerator {
  private final CachedCaptcha[] cached;
  private final int imageWidth, imageHeight;
  private final Gson gson;

  private static final Random RANDOM = new Random();

  public CaptchaGenerator(final Gson gson, final int precomputeAmount,
                          final int imageWidth, final int imageHeight) {
    this.gson = gson;
    this.cached = new CachedCaptcha[precomputeAmount];
    this.imageWidth = imageWidth;
    this.imageHeight = imageHeight;
    loadResources();
  }

  public void loadResources() {
    try {
      // Read all resources from the current context class loader
      final Enumeration<URL> enumeration = getClass().getClassLoader().getResources("captcha_mappings/");
      int loadedCaptchaCount = 0;

      if (enumeration.hasMoreElements()) {
        final URL url = enumeration.nextElement();
        final File file = new File(url.getFile());

        final String[] files = file.list();
        // This should not happen
        if (files == null) return;

        for (final String resourcePath : files) {
          final File mappingsFile = new File(file, resourcePath);
          // Only load valid mapping files
          if (!mappingsFile.isFile()) continue;

          try (final InputStream inputStream = Files.newInputStream(mappingsFile.toPath())) {
            try (final InputStreamReader reader = new InputStreamReader(inputStream)) {
              // Load the captcha properties from the file
              final CaptchaProperties properties = gson.fromJson(reader, CaptchaProperties.class);
              cached[loadedCaptchaCount++] = precompute(properties);
              // Don't continue the loop if we shouldn't compute more captchas
              if (loadedCaptchaCount >= cached.length) break;
            }
          } catch (IOException exception) {
            throw new IllegalStateException("Could not load resource", exception);
          }
        }
      }
    } catch (IOException exception) {
      throw new IllegalStateException("Could not find resources", exception);
    }
  }

  private CachedCaptcha precompute(final CaptchaProperties properties) {
    for (final String url : properties.getUrls()) {
    }
    return null; //TODO
  }

  public CachedCaptcha getRandomCaptcha() {
    return cached[RANDOM.nextInt(cached.length)];
  }
}
