package org.sourcegrade.jagr.gradle.task

import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.TaskAction
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.getByType
import java.util.Locale

@Suppress("LeakingThis")
abstract class GraderLibsTask : Jar(), GraderTask {

    @get:Input
    abstract override val graderName: Property<String>

    @get:Input
    abstract val assignmentId: Property<String>

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

    object Factory : GraderTask.Factory<GraderLibsTask> {
        override fun determineTaskName(name: String) = "${name}Libs"
        override fun configureTask(task: GraderLibsTask) {
            task.description = "Builds the grader libs jar for ${task.sourceSetName.get()}"
        }
    }
}
