import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.net.URI

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
      kotlinOptions.jvmTarget = "1.8"
    }
    withType<JavaCompile> {
      options.encoding = "UTF-8"
      sourceCompatibility = "1.8"
      targetCompatibility = "1.8"
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
  publishing {
    repositories {
      maven {
        credentials {
          username = project.findProperty("publishUserName") as? String
          password = project.findProperty("publishPassword") as? String
        }
        val releasesRepoUrl = "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
        val snapshotsRepoUrl = "https://s01.oss.sonatype.org/content/repositories/snapshots"
        url = URI(if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl)
      }
    }
    publications {
      create<MavenPublication>("maven") {
        from(components["java"])
        pom {
          name.set("JagrKt")
          description.set("An automated tool for grading programming assignments")
          url.set("https://www.jagrkt.org")
          scm {
            url.set("https://github.com/JagrKt/JagrKt")
            connection.set("scm:git:https://github.com/JagrKt/JagrKt.git")
            developerConnection.set("scm:git:https://github.com/JagrKt/JagrKt.git")
          }
          licenses {
            license {
              name.set("GNU LESSER GENERAL PUBLIC LICENSE Version 3")
              url.set("https://www.gnu.org/licenses/lgpl-3.0.html")
              distribution.set("repo")
            }
          }
          developers {
            developer {
              id.set("alexstaeding")
              name.set("Alexander Staeding")
            }
          }
        }
      }
    }
  }
}
