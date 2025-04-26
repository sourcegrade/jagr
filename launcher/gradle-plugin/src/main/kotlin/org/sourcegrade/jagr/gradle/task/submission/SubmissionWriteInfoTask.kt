package org.sourcegrade.jagr.gradle.task.submission

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.mapProperty
import org.sourcegrade.jagr.gradle.extension.JagrExtension
import org.sourcegrade.jagr.gradle.extension.SubmissionConfiguration
import org.sourcegrade.jagr.gradle.extension.createSubmissionInfoFileProperty
import org.sourcegrade.jagr.gradle.mergeSourceSets
import org.sourcegrade.jagr.gradle.task.JagrTaskFactory
import org.sourcegrade.jagr.gradle.task.WriteInfoTask
import org.sourcegrade.jagr.launcher.env.Jagr
import org.sourcegrade.jagr.launcher.io.RepositoryConfiguration
import org.sourcegrade.jagr.launcher.io.SourceSetInfo
import org.sourcegrade.jagr.launcher.io.SubmissionInfo

@Suppress("LeakingThis")
abstract class SubmissionWriteInfoTask : WriteInfoTask(), SubmissionTask {

    private val primaryContainer = project.extensions.getByType<JagrExtension>().submissions

    @get:Input
    val files: MapProperty<String, Map<String, Set<String>>> = project.objects.mapProperty<String, Map<String, Set<String>>>()
        .value(configurationName.map { c -> primaryContainer[c].sourceSets.mergeSourceSets() })

    @get:Input
    val dependencies: MapProperty<String, Set<String>> = project.objects.mapProperty<String, Set<String>>()
        .value(configurationName.map { c -> primaryContainer[c].getAllDependencies() })

    @get:OutputFile
    val submissionInfoFile: RegularFileProperty = createSubmissionInfoFileProperty(configurationName)

    init {
        group = "jagr resources"
        configureSubmissionCompilationDependency(configurationName.map { primaryContainer[it] })
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
There were some errors preparing your submission, please check your Gradle buildscript (e.g. build.gradle.kts).
The following required properties were not set:
$errors
""",
            )
        }
    }

    @TaskAction
    fun runTask() {
        val submissionInfo = SubmissionInfo(
            assignmentId.get(),
            Jagr.version,
            files.get().map { SourceSetInfo(it.key, it.value) },
            dependencies.get(),
            repositories.get().map { RepositoryConfiguration(it.first, it.second) },
            studentId.get(),
            firstName.get(),
            lastName.get(),
        )
        submissionInfoFile.get().asFile.apply {
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
