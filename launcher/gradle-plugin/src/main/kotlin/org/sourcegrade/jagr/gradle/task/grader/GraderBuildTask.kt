package org.sourcegrade.jagr.gradle.task.grader

import org.gradle.api.Project
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.tasks.InputFile
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.getByType
import org.sourcegrade.jagr.gradle.extension.GraderConfiguration
import org.sourcegrade.jagr.gradle.extension.JagrExtension
import org.sourcegrade.jagr.gradle.extension.createGraderInfoFileProperty
import org.sourcegrade.jagr.gradle.task.JagrTaskFactory

@Suppress("LeakingThis")
abstract class GraderBuildTask : Jar(), GraderTask {

    @get:InputFile
    val graderInfoFile: RegularFileProperty = createGraderInfoFileProperty()

    init {
        group = "build"
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        dependsOn(configurationName.map(GraderWriteInfoTask.Factory::determineTaskName))
        archiveFileName.set(graderName.map { "$it-${project.version}.jar" })
        from(graderInfoFile)
        from(configurationName.map { project.extensions.getByType<JagrExtension>().graders[it].getSourceDirectoriesRecursive() })
    }

    private fun GraderConfiguration.getSourceDirectoriesRecursive(): List<SourceDirectorySet> {
        val result = mutableListOf<SourceDirectorySet>()
        sourceSets.forEach { result.add(it.allSource) }
        // technically this is a race condition, but we can't use Provider.zip because the value is not always configured
        if (solutionConfiguration.isPresent) {
            solutionConfiguration.get().sourceSets.asSequence()
                .map { it.allSource }
                .forEach { result.add(it) }
        }
        if (parentConfiguration.isPresent) {
            result.addAll(parentConfiguration.get().getSourceDirectoriesRecursive())
        }
        return result
    }

    internal object Factory : JagrTaskFactory<GraderBuildTask, GraderConfiguration> {
        override fun determineTaskName(name: String) = "${name}BuildGrader"
        override fun configureTask(task: GraderBuildTask, project: Project, configuration: GraderConfiguration) {
            task.description = "Builds the grader jar for ${task.sourceSetNames.get()}"
        }
    }
}
