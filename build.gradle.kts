buildscript {
  dependencies {
    classpath("gradle.plugin.io.toolebox:gradle-git-versioner:1.6.7")
  }
}

plugins {
  id("java")
  id("io.github.goooler.shadow") version "8.1.7"
  id("io.toolebox.git-versioner") version "1.6.7"
}

apply(plugin = "io.toolebox.git-versioner")

versioner {
  pattern {
    pattern = "$version-%h-%c-%b"
  }
}

repositories {
  mavenCentral() // Lombok
}

apply(plugin = "java")
apply(plugin = "io.github.goooler.shadow")

dependencies {
  compileOnly("org.projectlombok:lombok:1.18.32")
  annotationProcessor("org.projectlombok:lombok:1.18.32")

  testCompileOnly("org.projectlombok:lombok:1.18.32")
  testAnnotationProcessor("org.projectlombok:lombok:1.18.32")

  implementation("com.jhlabs:filters:2.0.235-1")
  compileOnly("org.jetbrains:annotations:24.0.1")
}

tasks {
  jar {
    manifest {
      // Set the implementation version, so we can create exact version
      // information in-game and make it accessible to the user.
      attributes["Implementation-Version"] = version
      attributes["Implementation-Vendor"] = "Jones Development"
    }
  }

  shadowJar {
    minimize()

    // Set the file name of the shadowed jar
    archiveFileName.set("${rootProject.name}-${rootProject.version}.jar")

    // Remove file timestamps
    isPreserveFileTimestamps = false

    // Relocate libraries
    relocate("com.jhlabs", "xyz.jonesdev.capja.libs.jhlabs")

    // Exclude unnecessary metadata information
    exclude("META-INF/*/**")
  }

  compileJava {
    options.encoding = "UTF-8"
  }

  // This is a small wrapper tasks to simplify the building process
  register("build-captcha") {
    dependsOn(clean, shadowJar)
  }
}

java.sourceCompatibility = JavaVersion.VERSION_11
java.targetCompatibility = JavaVersion.VERSION_11
