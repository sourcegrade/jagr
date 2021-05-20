/*
 *   JagrKt - JagrKt.org
 *   Copyright (C) 2021 Alexander Staeding
 *   Copyright (C) 2021 Contributors
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.jagrkt.script

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.get
import java.net.URI

class JagrKtPublishPlugin : Plugin<Project> {
  override fun apply(target: Project) = target.afterEvaluate { configure() }
  private fun Project.configure() {
    apply<JavaBasePlugin>()
    apply<MavenPublishPlugin>()
    extensions.configure<JavaPluginExtension> {
      withJavadocJar()
      withSourcesJar()
    }
    extensions.configure<PublishingExtension> {
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
}
