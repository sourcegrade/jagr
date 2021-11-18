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
  implementation("com.github.ajalt.clikt:clikt:3.3.0")
}

application {
  mainClass.set("org.sourcegrade.jagr.MainKt")
}

tasks {
  named<JavaExec>("run") {
    doFirst {
      error("Use runShadow instead")
    }
  }
  named<JavaExec>("runShadow") {
    workingDir = File("build/run").also(File::mkdirs)
  }
  jar {
    enabled = false
  }
  shadowJar {
    from("gradlew") {
      into("org/gradle")
    }
    from("gradlew.bat") {
      into("org/gradle")
    }
    from("gradle/wrapper/gradle-wrapper.properties") {
      into("org/gradle")
    }
    archiveFileName.set("Jagr-${project.version}.jar")
  }
}

project.extra["apiVersion"] = "0.3-SNAPSHOT"

allprojects {
  group = "org.sourcegrade"
  version = "0.2.1-SNAPSHOT"

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
