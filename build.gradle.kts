import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  `maven-publish`
  val kotlinVersion = "1.4.31"
  kotlin("jvm").version(kotlinVersion)
  kotlin("plugin.serialization").version(kotlinVersion)
  id("com.github.johnrengelman.shadow").version("6.1.0")
}

allprojects {
  group = "org.jagrkt"
  version = "0.1.0-SNAPSHOT"

  repositories {
    mavenCentral()
  }

  tasks {
    withType<KotlinCompile> {
      kotlinOptions.jvmTarget = "15"
    }
    withType<JavaCompile> {
      options.encoding = "UTF-8"
    }
  }
}

project("jagrkt-api") {
  apply(plugin = "java-library")
  apply(plugin = "maven-publish")
  java {
    withJavadocJar()
    withSourcesJar()
  }
  afterEvaluate {
    publishing {
      publications {
        create<MavenPublication>("maven") {
          from(components["java"])
        }
      }
    }
  }
}
