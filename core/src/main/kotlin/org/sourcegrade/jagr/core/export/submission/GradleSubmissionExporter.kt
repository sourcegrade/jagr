/*
 *   Jagr - SourceGrade.org
 *   Copyright (C) 2021-2022 Alexander Staeding
 *   Copyright (C) 2021-2022 Contributors
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
import org.apache.logging.log4j.Logger
import org.sourcegrade.jagr.api.testing.Submission
import org.sourcegrade.jagr.core.compiler.java.JavaSourceContainer
import org.sourcegrade.jagr.core.testing.GraderJarImpl
import org.sourcegrade.jagr.core.testing.JavaSubmission
import org.sourcegrade.jagr.launcher.io.GraderJar
import org.sourcegrade.jagr.launcher.io.ResourceContainer
import org.sourcegrade.jagr.launcher.io.SourceSetInfo
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
        val buildScript = if (graderJar == null) null else {
            graderJar.configuration.exportBuildScriptPath?.let { path -> path to graderJar.container.source.resources[path] }
        }
        buildScript?.second?.let { resource ->
            addResource {
                name = "build.gradle.kts"
                resource.inputStream().copyTo(outputStream)
            }
        } ?: run {
            if (buildScript != null) {
                logger.error(
                    "Build script '${buildScript.first}' specified in grader configuration does not exist, using default"
                )
            }
            writeGradleResource(resource = "build.gradle.kts_", targetName = "build.gradle.kts")
        }
        val filteredSubmissions = if (graderJar == null) {
            submissions
        } else {
            submissions.filter { graderJar.info.assignmentId == (it as JavaSubmission).submissionInfo.assignmentId }
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
        writeGradleResource(classLoader, resource = "gradle-wrapper.properties", targetDir = "gradle/wrapper/")
    }

    private fun ResourceContainer.Builder.writeSettings(graderName: String, submissions: List<Submission>) = addResource {
        name = "settings.gradle.kts"
        PrintWriter(outputStream, false, Charsets.UTF_8).use { printer ->
            """
                dependencyResolutionManagement {
                    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
                    repositories {
                        mavenLocal()
                        maven("https://s01.oss.sonatype.org/content/repositories/snapshots")
                        mavenCentral()
                    }
                }

                pluginManagement {
                    repositories {
                        mavenLocal()
                        maven("https://s01.oss.sonatype.org/content/repositories/snapshots")
                        mavenCentral()
                        gradlePluginPortal()
                    }
                }
            """.trimIndent().also { printer.println(it) }
            printer.appendLine("rootProject.name = \"$graderName\"")
            submissions.forEach { submission ->
                printer.appendLine("include(\"${submission.info}\")")
            }
            printer.appendLine()
            printer.flush()
        }
    }

    private fun ResourceContainer.Builder.writeSubmission(submission: Submission, graderJar: GraderJarImpl?) {
        if (submission !is JavaSubmission) return
        val submissionName = submission.submissionInfo.toString()
        writeFiles(submissionName, submission.submissionInfo.sourceSets, submission.compileResult.source)
        if (graderJar != null) {
            writeFiles(
                submissionName,
                graderJar.info.sourceSets.filter { a -> submission.submissionInfo.sourceSets.none { b -> a.name == b.name } },
                graderJar.containerWithoutSolution.source
            )
        }
        val sInfo = submission.submissionInfo
        addResource {
            name = "$submissionName/build.gradle.kts"
            PrintWriter(outputStream, false, Charsets.UTF_8).use { printer ->
                """
                jagr {
                    assignmentId.set("h00")
                    submissions {
                        val main by creating {
                            studentId.set("${sInfo.studentId}")
                            firstName.set("${sInfo.firstName}")
                            lastName.set("${sInfo.lastName}")
                        }
                    }
                """.trimIndent().also { printer.println(it) }
                if (graderJar != null) {
                    val gInfo = graderJar.info
                    """
                    graders {
                        val grader by creating {
                            graderName.set("${gInfo.name}")
                            rubricProviderName.set("${gInfo.rubricProviderName}")
                        }
                    }
                    """.trimIndent().prependIndent(" ".repeat(4)).also { printer.println(it) }
                }
                printer.println("}\n")
                val graderDeps = graderJar?.info?.dependencyConfigurations?.formatDependencies() ?: emptySet()
                val submissionDeps = sInfo.dependencyConfigurations.formatDependencies() - graderDeps
                if (graderDeps.isNotEmpty() || submissionDeps.isNotEmpty()) {
                    printer.println("dependencies {")
                    printer.println("    project.afterEvaluate {")
                    for (graderDep in graderDeps) {
                        printer.println("        $graderDep")
                    }
                    if (submissionDeps.isNotEmpty()) {
                        """
                        /*
                         * ATTENTION:
                         * the following dependencies were added manually by the student
                         * you may uncomment the following lines to add them to the compilation classpath
                         */
                        """.trimIndent().prependIndent(" ".repeat(8)).also { printer.println(it) }
                        for (submissionDep in submissionDeps) {
                            printer.println("//        $submissionDep")
                        }
                    }
                    printer.println("    }")
                    printer.println("}")
                }
            }
        }
    }

    private fun ResourceContainer.Builder.writeFiles(
        submissionName: String,
        sourceSets: List<SourceSetInfo>,
        source: JavaSourceContainer,
    ) {
        for (sourceSet in sourceSets) {
            sourceSet.files.asSequence()
                .flatMap { (directorySet, files) -> files.map { directorySet to it } }
                .forEach { (directorySet, fileName) ->
                    if (fileName == "submission-info.json") {
                        return@forEach
                    }
                    addResource {
                        name = "$submissionName/src/${sourceSet.name}/$directorySet/$fileName"
                        if (directorySet == "resources") {
                            source.resources[fileName]
                                ?.also { resource -> outputStream.writeBytes(resource) }
                                ?: logger.error("Resource $fileName not found in submission $submissionName")
                        } else {
                            source.sourceFiles[fileName]
                                ?.also { sourceFile -> outputStream.writer().use { it.write(sourceFile.content) } }
                                ?: logger.error("Source file $fileName not found in submission $submissionName")
                        }
                    }
                }
        }
    }

    private fun Map<String, Set<String>>.formatDependencies(): Set<String> {
        return flatMap { (sourceSet, dependencies) -> dependencies.associateBy { sourceSet }.toList() }
            .mapTo(mutableSetOf()) { "${it.first}(\"${it.second}\")" }
    }

    companion object {
        const val DEFAULT_EXPORT_NAME = "default"
    }
}
