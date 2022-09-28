package org.sourcegrade.jagr.gradle.task.grader

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.listProperty
import org.gradle.kotlin.dsl.property
import org.sourcegrade.jagr.gradle.GraderConfiguration
import org.sourcegrade.jagr.gradle.JagrExtension
import org.sourcegrade.jagr.gradle.forEachFile
import org.sourcegrade.jagr.gradle.getFiles
import org.sourcegrade.jagr.gradle.task.JagrTaskFactory
import org.sourcegrade.jagr.launcher.VersionProvider
import org.sourcegrade.jagr.launcher.io.GraderInfo
import org.sourcegrade.jagr.launcher.io.SourceSetInfo
import java.io.File

@Suppress("LeakingThis")
abstract class GraderWriteInfoTask : DefaultTask(), GraderTask {

    @get:Input
    val graderFiles: ListProperty<String> = project.objects.listProperty<String>().value(
        configurationName.map { configuration ->
            project.extensions.getByType<JagrExtension>().graders[configuration].getFilesRecursive()
        }
    )

    @get:Input
    val solutionFiles: ListProperty<String> = project.objects.listProperty<String>().value(
        solutionConfigurationName.map { configuration ->
            project.extensions.getByType<JagrExtension>().submissions[configuration].sourceSets.flatMap { it.getFiles() }
        }
    )

    @get:OutputFile
    val graderInfoFile: Property<File> = project.objects.property<File>()
        .value(configurationName.map { project.buildDir.resolve("resources/jagr/$it/grader-info.json") })

    init {
        group = "jagr resources"
        // TODO: Depend only on compilation task from required source sets
        dependsOn("compileJava")
    }

    private fun GraderConfiguration.getFilesRecursive(): List<String> {
        val result = mutableListOf<String>()
        sourceSets.forEach { sourceSet ->
            sourceSet.forEachFile { result.add(it) }
        }
        // technically this is a race condition, but we can't use Provider.zip because the value is not always configured
        if (parentConfiguration.isPresent) {
            result.addAll(parentConfiguration.get().getFilesRecursive())
        }
        return result
    }

    @TaskAction
    fun runTask() {
        val graderInfo = GraderInfo(
            assignmentId.get(),
            VersionProvider.version,
            graderName.get(),
            listOf(
                SourceSetInfo("grader", graderFiles.get()),
                SourceSetInfo("solution", solutionFiles.get())
            ),
        )
        graderInfoFile.get().apply {
            parentFile.mkdirs()
            writeText(Json.encodeToString(graderInfo))
        }
    }

    internal object Factory : JagrTaskFactory<GraderWriteInfoTask, GraderConfiguration> {
        override fun determineTaskName(name: String) = "${name}WriteGraderInfo"
        override fun configureTask(task: GraderWriteInfoTask, project: Project, configuration: GraderConfiguration) {
            task.description = "Runs the ${task.sourceSetNames.get()} grader"
            task.assignmentId.set(project.extensions.getByType<JagrExtension>().assignmentId)
        }
    }
}
