package org.sourcegrade.jagr.gradle.task.grader

import org.gradle.api.Project
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.TaskAction
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.getByType
import org.sourcegrade.jagr.gradle.GraderConfiguration
import org.sourcegrade.jagr.gradle.task.JagrTaskFactory
import java.util.Locale

@Suppress("LeakingThis")
abstract class GraderLibsTask : Jar(), GraderTask {

    init {
        group = "build"
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        archiveFileName.set(graderName.map { "$it-libs.jar" })
    }

    @TaskAction
    fun runTask() {
        // don't include Jagr's runtime dependencies
        val jagrRuntime = project.configurations["graderPrivateCompileClasspath"]
            .resolvedConfiguration
            .firstLevelModuleDependencies
            .first { it.moduleGroup == "org.sourcegrade" && it.moduleName == "jagr-launcher" }
            .allModuleArtifacts
            .map { it.file }

        val runtimeDeps = project.extensions
            .getByType<SourceSetContainer>()
            .getByName("graderPrivate")
            .runtimeClasspath.mapNotNull {
                if (it.path.lowercase(Locale.getDefault()).contains(assignmentId.get()) || jagrRuntime.contains(it)) {
                    null
                } else if (it.isDirectory) {
                    it
                } else {
                    project.zipTree(it)
                }
            }
        from(runtimeDeps)
    }

    internal object Factory : JagrTaskFactory<GraderLibsTask, GraderConfiguration> {
        override fun determineTaskName(name: String) = "${name}Libs"
        override fun configureTask(task: GraderLibsTask, project: Project, configuration: GraderConfiguration) {
            task.description = "Builds the grader libs jar for ${task.sourceSetNames.get()}"
        }
    }
}
