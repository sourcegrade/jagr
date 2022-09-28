package org.sourcegrade.jagr.gradle.task.submission

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.mapProperty
import org.gradle.kotlin.dsl.property
import org.sourcegrade.jagr.gradle.JagrExtension
import org.sourcegrade.jagr.gradle.SubmissionConfiguration
import org.sourcegrade.jagr.gradle.getFiles
import org.sourcegrade.jagr.gradle.task.JagrTaskFactory
import org.sourcegrade.jagr.launcher.io.SourceSetInfo
import org.sourcegrade.jagr.launcher.io.SubmissionInfo
import java.io.File

@Suppress("LeakingThis")
abstract class SubmissionWriteInfoTask : DefaultTask(), SubmissionTask {

    @get:Input
    val sourceSetFiles: MapProperty<String, List<String>> = project.objects.mapProperty<String, List<String>>().value(
        configurationName.map { configuration ->
            project.extensions.getByType<JagrExtension>().submissions[configuration].sourceSets.associate {
                it.name to it.getFiles()
            }
        }
    )

    @get:OutputFile
    val submissionInfoFile: Property<File> = project.objects.property<File>()
        .value(configurationName.map { project.buildDir.resolve("resources/jagr/$it/submission-info.json") })

    init {
        group = "jagr resources"
        dependsOn("compileJava")
        // TODO: Cleaner way of throwing error
        setOnlyIf {
            verifySubmit()
            true
        }
    }

    private fun verifySubmit() {
        val errors = buildString {
            if (!assignmentId.isPresent) appendLine("assignmentId")
            if (!studentId.isPresent) appendLine("studentId")
            if (!firstName.isPresent) appendLine("firstName")
            if (!lastName.isPresent) appendLine("lastName")
        }
        if (errors.isNotEmpty()) {
            throw GradleException(
                """
There were some errors preparing your submission. The following required properties were not set:
$errors
"""
            )
        }
    }

    @TaskAction
    fun runTask() {
        val submissionInfo = SubmissionInfo(
            assignmentId.get(),
            studentId.get(),
            firstName.get(),
            lastName.get(),
            sourceSetFiles.get().map { SourceSetInfo(it.key, it.value) },
        )
        submissionInfoFile.get().apply {
            parentFile.mkdirs()
            writeText(Json.encodeToString(submissionInfo))
        }
    }

    internal object Factory : JagrTaskFactory<SubmissionWriteInfoTask, SubmissionConfiguration> {
        override fun determineTaskName(name: String) = "${name}WriteSubmissionInfo"
        override fun configureTask(task: SubmissionWriteInfoTask, project: Project, configuration: SubmissionConfiguration) {
            task.description = "Writes the submission info to a file"
        }
    }
}
