<div align="center">
  <!-- Introduction -->
  <p>
    <h1>🤖 capja</h1>
    Easy-to-use Java CAPTCHA image generation API used by Sonar
  </p>
  
  <!-- Badges & icons -->
  [![](https://www.codefactor.io/repository/github/jonesdevelopment/capja/badge/main)](https://www.codefactor.io/repository/github/jonesdevelopment/capja/overview/main)
  [![](https://img.shields.io/github/v/release/jonesdevelopment/capja)](https://github.com/jonesdevelopment/capja/releases)
  [![](https://img.shields.io/github/issues/jonesdevelopment/capja)](https://github.com/jonesdevelopment/capja/issues)
  [![](https://img.shields.io/discord/923308209769426994.svg?logo=discord)](https://jonesdev.xyz/discord)
  [![](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)
  <br>
  <br>
  <!-- Quick navigation -->
  [Releases](https://github.com/jonesdevelopment/capja/releases)
  |
  [Issues](https://github.com/jonesdevelopment/capja/issues)
  |
  [Pull Requests](https://github.com/jonesdevelopment/capja/pulls)
  |
  [Discord](https://jonesdev.xyz/discord)
  |
  [License](https://github.com/jonesdevelopment/sonar/?tab=readme-ov-file#license)
</div>

## Examples

#### capja being used in Minecraft as a CAPTCHA
https://youtu.be/Aw1w-7S0GjA

#### capja being integrated in Sonar
[CaptchaPreparer.java](<https://github.com/jonesdevelopment/sonar/blob/main/sonar-common/src/main/java/xyz/jonesdev/sonar/common/fallback/protocol/captcha/CaptchaPreparer.java>) (using custom filters)

## Usage

```java
import java.awt.image.BufferedImage;

// Creates a CAPTCHA generator instance
simpleCaptchaGenerator = new SimpleCaptchaGenerator(width, height, null);

// You can also use a custom background image
simpleCaptchaGenerator = new SimpleCaptchaGenerator(width, height, new File("background.png"));

// Creates a BufferedImage with the code 69420
bufferedImage = simpleCaptchaGenerator.createImage(new char[]{'6', '9', '4', '2', '0'});

// You can also use custom effects and filters
bufferedImage = simpleCaptchaGenerator.createImage(/* ... */, new BumpFilter(), new SmearFilter());
```

## License

capja is licensed under the [GNU General Public License 3.0](https://www.gnu.org/licenses/gpl-3.0.en.html).

## Credits

- Special thanks to [jhlabs](<http://www.jhlabs.com/ip/filters/>) for creating awesome image filters
