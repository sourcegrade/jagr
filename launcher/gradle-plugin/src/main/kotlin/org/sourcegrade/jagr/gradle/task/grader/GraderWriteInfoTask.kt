package org.sourcegrade.jagr.gradle.task.grader

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.property
import org.sourcegrade.jagr.gradle.GraderConfiguration
import org.sourcegrade.jagr.gradle.GraderInfo
import org.sourcegrade.jagr.gradle.JagrExtension
import org.sourcegrade.jagr.gradle.SourceSetInfo
import org.sourcegrade.jagr.gradle.forEachFile
import org.sourcegrade.jagr.gradle.getFiles
import java.io.File

@Suppress("LeakingThis")
abstract class GraderWriteInfoTask : DefaultTask(), GraderTask {

    @get:OutputFile
    val graderInfoFile: Property<File> = project.objects.property<File>()
        .value(configurationName.map { project.buildDir.resolve("resources/jagr/$it/grader-info.json") })

    init {
        // TODO: Depend only on compilation task from required source sets
        dependsOn("compileJava")
        group = "jagr"
    }

    private fun GraderConfiguration.getFilesRecursive(): List<String> {
        val result = mutableListOf<String>()
        sourceSets.forEach { sourceSet ->
            sourceSet.forEachFile { result.add(it) }
        }
        if (parentConfiguration.isPresent) {
            result.addAll(parentConfiguration.get().getFilesRecursive())
        }
        return result;
    }

    @TaskAction
    fun runTask() {
        val jagr = project.extensions.getByType<JagrExtension>()
        val graderFiles = jagr.graders[configurationName.get()].getFilesRecursive()
        val solutionFiles = jagr.submissions[solutionConfigurationName.get()].sourceSets.flatMap { it.getFiles() }
        val graderInfo = GraderInfo(
            graderName.get(),
            assignmentId.get(),
            listOf(
                SourceSetInfo("grader", graderFiles),
                SourceSetInfo("solution", solutionFiles)
            ),
        )
        graderInfoFile.get().apply {
            parentFile.mkdirs()
            writeText(Json.encodeToString(graderInfo))
        }
    }

    object Factory : GraderTask.Factory<GraderWriteInfoTask> {
        override fun determineTaskName(name: String) = "${name}WriteInfo"
        override fun configureTask(task: GraderWriteInfoTask, project: Project, grader: GraderConfiguration) {
            task.description = "Runs the ${task.sourceSetNames.get()} grader"
            task.assignmentId.set(project.extensions.getByType<JagrExtension>().assignmentId)
        }
    }
}
