import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.sourcegrade.jagr.script.JagrPublishPlugin

plugins {
  application
  kotlin("jvm")
  id("com.github.johnrengelman.shadow")
}

dependencies {
  runtimeOnly(project("jagr-core"))
  implementation(project("jagr-launcher"))
  implementation("com.github.ajalt.clikt:clikt:3.2.0")
}

application {
  mainClass.set("org.sourcegrade.jagr.MainKt")
}

tasks {
  jar {
    enabled = false
  }
  shadowJar {
    archiveFileName.set("Jagr-${project.version}.jar")
  }
}

allprojects {
  group = "org.sourcegrade"
  version = "0.1.0-SNAPSHOT"

  project.findProperty("buildNumber")
    ?.takeIf { version.toString().contains("SNAPSHOT") }
    ?.also { version = version.toString().replace("SNAPSHOT", "RC$it") }

  repositories {
    mavenCentral()
  }

  tasks {
    withType<KotlinCompile> {
      kotlinOptions.jvmTarget = "11"
    }
    withType<JavaCompile> {
      options.encoding = "UTF-8"
      sourceCompatibility = "11"
      targetCompatibility = "11"
    }
  }
}

subprojects {
  apply<JagrPublishPlugin>()
}
