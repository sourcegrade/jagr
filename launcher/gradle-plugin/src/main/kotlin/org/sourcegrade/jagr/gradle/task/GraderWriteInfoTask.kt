package org.sourcegrade.jagr.gradle.task

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.listProperty
import org.sourcegrade.jagr.gradle.GraderInfo
import org.sourcegrade.jagr.gradle.GraderSourceSetConfiguration
import org.sourcegrade.jagr.gradle.JagrExtension
import org.sourcegrade.jagr.gradle.SourceSetInfo
import org.sourcegrade.jagr.gradle.toInfo

@Suppress("LeakingThis")
abstract class GraderWriteInfoTask : DefaultTask(), GraderTask, TargetAssignmentTask {

    @get:Input
    internal val sourceSets: ListProperty<SourceSetInfo> = project.objects.listProperty<SourceSetInfo>()
        .convention(project.extensions.getByType<SourceSetContainer>().map { it.toInfo() })

    @get:OutputFile
    val graderInfoFile = project.buildDir.resolve("resources/jagr/grader-info.json")

    init {
        dependsOn("compileJava")
        group = "jagr"
    }

    @TaskAction
    fun runTask() {
        val graderInfo = GraderInfo(
            graderName.get(),
            assignmentId.get(),
            // TODO: Clean this up
            sourceSets.get()
                .partition { it.name.contains("grader") }
                .let { p ->
                    p.first.fold(SourceSetInfo("grader", emptyList())) { acc, sourceSetInfo ->
                        acc.copy(files = acc.files + sourceSetInfo.files)
                    } to p.second.fold(SourceSetInfo("solution", emptyList())) { acc, sourceSetInfo ->
                        acc.copy(files = acc.files + sourceSetInfo.files)
                    }
                }
                .toList(),
        )
        graderInfoFile.apply {
            parentFile.mkdirs()
            writeText(Json.encodeToString(graderInfo))
        }
    }

    object Factory : GraderTask.Factory<GraderWriteInfoTask> {
        override fun determineTaskName(name: String) = "${name}WriteInfo"
        override fun configureTask(task: GraderWriteInfoTask, project: Project, grader: GraderSourceSetConfiguration) {
            task.description = "Runs the ${task.sourceSetName.get()} grader"
            task.assignmentId.set(project.extensions.getByType<JagrExtension>().assignmentId)
        }
    }
}
