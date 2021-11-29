/*
 *   Jagr - SourceGrade.org
 *   Copyright (C) 2021 Alexander Staeding
 *   Copyright (C) 2021 Contributors
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.sourcegrade.jagr.core.export.submission

import com.google.common.collect.FluentIterable
import com.google.common.reflect.ClassPath
import com.google.inject.Inject
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.slf4j.Logger
import org.sourcegrade.jagr.api.testing.SourceFile
import org.sourcegrade.jagr.api.testing.Submission
import org.sourcegrade.jagr.core.testing.GraderJarImpl
import org.sourcegrade.jagr.core.testing.JavaSubmission
import org.sourcegrade.jagr.core.testing.SubmissionInfoImpl
import org.sourcegrade.jagr.launcher.io.GraderJar
import org.sourcegrade.jagr.launcher.io.ResourceContainer
import org.sourcegrade.jagr.launcher.io.SubmissionExporter
import org.sourcegrade.jagr.launcher.io.addResource
import org.sourcegrade.jagr.launcher.io.buildResourceContainer
import org.sourcegrade.jagr.launcher.io.buildResourceContainerInfo
import org.sourcegrade.jagr.launcher.io.createResourceBuilder
import java.io.PrintWriter
import java.util.jar.Attributes
import java.util.jar.JarEntry
import java.util.jar.JarOutputStream
import java.util.jar.Manifest

class GradleSubmissionExporter @Inject constructor(
  private val logger: Logger,
) : SubmissionExporter.Gradle {
  override fun export(graders: List<GraderJar>, submissions: List<Submission>): List<ResourceContainer> {
    val result = ArrayList<ResourceContainer>(graders.size + 1)
    graders.mapTo(result) { export(it, submissions) }
    result += export(null, submissions)
    return result
  }

  private fun export(graderJar: GraderJar?, submissions: List<Submission>) = buildResourceContainer {
    graderJar as GraderJarImpl?
    info = buildResourceContainerInfo {
      name = graderJar?.info?.name ?: DEFAULT_EXPORT_NAME
    }
    writeSkeleton()
    val filteredSubmissions = if (graderJar == null) {
      submissions
    } else {
      submissions.filter { graderJar.rubricProviders.containsKey(it.info.assignmentId) }
    }
    for (submission in filteredSubmissions) {
      writeSubmission(submission, graderJar)
    }
    writeSettings(info.name, filteredSubmissions)
  }

  private fun ResourceContainer.Builder.writeGradleResource(
    classLoader: ClassLoader = ClassLoader.getSystemClassLoader(),
    resource: String,
    targetDir: String = "",
    targetName: String = resource,
  ) = addResource {
    name = targetDir + targetName
    classLoader.getResourceAsStream("org/gradle/$resource")?.also {
      it.copyTo(outputStream)
    } ?: logger.error("Could not read gradle resource: $resource")
  }

  @Suppress("UnstableApiUsage")
  private fun ResourceContainer.Builder.writeSkeleton() {
    val classLoader = ClassLoader.getSystemClassLoader()
    val wrapperClasses: Set<ClassPath.ClassInfo> = FluentIterable.from(ClassPath.from(classLoader).resources)
      .filter(ClassPath.ClassInfo::class.java)
      .filter { it!!.packageName.startsWith("org.gradle") }
      .toSet()
    val wrapperBuilder = createResourceBuilder()
    wrapperBuilder.name = "gradle/wrapper/gradle-wrapper.jar"
    val manifest = Manifest()
    manifest.mainAttributes[Attributes.Name.MANIFEST_VERSION] = "1.0"
    manifest.mainAttributes[Attributes.Name.IMPLEMENTATION_TITLE] = "Gradle Wrapper"
    JarOutputStream(wrapperBuilder.outputStream, manifest).use { jar ->
      for (wrapperClass in wrapperClasses) {
        val className = "${wrapperClass.name.replace('.', '/')}.class"
        val classStream = classLoader.getResourceAsStream(className)
        if (classStream == null) { // opened over *9* years ago, sigh https://youtrack.jetbrains.com/issue/KT-1436
          logger.error("Unable to read gradle wrapper class $className")
          continue
        }
        val entry = JarEntry(className)
        jar.putNextEntry(entry)
        jar.write(classStream.use { it.readBytes() })
        jar.closeEntry()
      }
    }
    addResource(wrapperBuilder.build())
    writeGradleResource(classLoader, resource = "gradlew")
    writeGradleResource(classLoader, resource = "gradlew.bat")
    writeGradleResource(classLoader, resource = "build.gradle.kts_", targetName = "build.gradle.kts")
    writeGradleResource(classLoader, resource = "gradle-wrapper.properties", targetDir = "gradle/wrapper/")
  }

  private fun ResourceContainer.Builder.writeSettings(graderName: String, submissions: List<Submission>) = addResource {
    name = "settings.gradle.kts"
    PrintWriter(outputStream, false, Charsets.UTF_8).use {
      it.appendLine("rootProject.name = \"$graderName\"")
      submissions.forEach { submission ->
        it.appendLine("include(\"${submission.info}\")")
      }
      it.appendLine()
      it.flush()
    }
  }

  private fun ResourceContainer.Builder.writeSubmission(submission: Submission, graderJar: GraderJarImpl?) {
    if (submission !is JavaSubmission) return
    val submissionName = submission.info.toString()
    addResource {
      name = "$submissionName/src/main/resources/submission-info.json"
      outputStream.writer().use { it.write(Json.encodeToString(submission.info as SubmissionInfoImpl)) }
    }
    writeSourceFiles("$submissionName/src/main/java/", submission.compileResult.source.sourceFiles)
    writeResources("$submissionName/src/main/resources/", submission.compileResult.runtimeResources.resources)
    if (graderJar != null) {
      writeSourceFiles("$submissionName/src/test/java/", graderJar.containerWithoutSolution.source.sourceFiles)
      writeResources("$submissionName/src/test/resources/", graderJar.containerWithoutSolution.runtimeResources.resources)
    }
  }

  private fun ResourceContainer.Builder.writeSourceFiles(prefix: String, sourceFiles: Map<String, SourceFile>) {
    for ((fileName, sourceFile) in sourceFiles) {
      addResource {
        name = prefix + fileName
        outputStream.writer().use { it.write(sourceFile.content) }
      }
    }
  }

  private fun ResourceContainer.Builder.writeResources(prefix: String, resources: Map<String, ByteArray>) {
    for ((fileName, resource) in resources) {
      addResource {
        name = prefix + fileName
        outputStream.writeBytes(resource)
      }
    }
  }

  companion object {
    const val DEFAULT_EXPORT_NAME = "default"
  }
}
