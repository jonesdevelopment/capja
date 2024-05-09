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

package xyz.jonesdev.capja.test;

import com.jhlabs.image.BumpFilter;
import com.jhlabs.image.SmearFilter;
import lombok.experimental.UtilityClass;
import xyz.jonesdev.capja.SimpleCaptchaGenerator;
import xyz.jonesdev.capja.filter.SimpleRippleFilter;
import xyz.jonesdev.capja.filter.TransparentScratchFilter;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;

@UtilityClass
public class Main {
  public void main(final String... args) throws IOException {
    final SimpleCaptchaGenerator simpleCaptchaGenerator = new SimpleCaptchaGenerator(128, 128, null);

    // Sine wave ripple effect
    final SimpleRippleFilter rippleFilter = new SimpleRippleFilter();
    rippleFilter.setXAmplitude(0);
    float yAmplitude = 10 - ThreadLocalRandom.current().nextInt(20);
    if (Math.abs(yAmplitude) < 3) {
      yAmplitude = yAmplitude >= 0 ? 3 : -3;
    }
    rippleFilter.setYAmplitude(yAmplitude);

    // Filters for general distortion
    final SmearFilter smearFilter = new SmearFilter();
    smearFilter.setShape(SmearFilter.CIRCLES);
    smearFilter.setMix(0.15f);
    smearFilter.setDensity(0.1f);
    smearFilter.setDistance(5);

    // Scratches
    final TransparentScratchFilter scratchFilter = new TransparentScratchFilter(5);

    final long start = System.currentTimeMillis();
    final char[] dictionary = {'1', '2', '3', '5', '6', '8', '9'};
    final char[] answer = new char[5];
    for (int j = 0; j < answer.length; j++) {
      answer[j] = dictionary[ThreadLocalRandom.current().nextInt(dictionary.length)];
    }

    // Test custom gradient color
    // If this isn't given, it'll just use the inverse color of the background at the pixel
    {
      final Color color0 = Color.getHSBColor((float) Math.random(), 1, 1);
      final Color color1 = Color.getHSBColor((float) Math.random(), 1, 0.5f);
      final GradientPaint gradient = new GradientPaint(0, 0, color0,
        simpleCaptchaGenerator.getWidth(), simpleCaptchaGenerator.getHeight(), color1);
      simpleCaptchaGenerator.setGradient(gradient);
    }

    // Generate image
    final BufferedImage bufferedImage = simpleCaptchaGenerator.createImage(answer,
      new BumpFilter(), scratchFilter, rippleFilter, smearFilter);
    System.out.println("Took " + (System.currentTimeMillis() - start) + "ms to create image(s)");

    // Save image
    ImageIO.write(bufferedImage, "png", new File("1.png"));
  }
}
