package org.sourcegrade.jagr.gradle.task.grader

import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.property
import org.sourcegrade.jagr.gradle.GraderConfiguration
import org.sourcegrade.jagr.gradle.task.JagrTaskFactory
import java.io.File

@Suppress("LeakingThis")
abstract class GraderBuildTask : Jar(), GraderTask {

    @get:InputFile
    val graderInfoFile: Property<File> = project.objects.property<File>()
        .value(configurationName.map { project.buildDir.resolve("resources/jagr/$it/grader-info.json") })

    init {
        dependsOn(configurationName.map(GraderWriteInfoTask.Factory::determineTaskName))
        archiveFileName.set(graderName.map { "$it-${project.version}.jar" })
        from(graderInfoFile)
        val sourceSets = project.extensions.getByType<SourceSetContainer>()
        from(solutionConfigurationName.map { sourceSets[it].allSource })
        from(
            sourceSetNames.zip(dependentConfigurationNames) { a, b -> a + b }
                .map { names -> names.map { name -> sourceSets[name].allSource } }
        )
    }

    internal object Factory : JagrTaskFactory<GraderBuildTask, GraderConfiguration> {
        override fun determineTaskName(name: String) = "${name}Build"
        override fun configureTask(task: GraderBuildTask, project: Project, configuration: GraderConfiguration) {
            task.description = "Builds the grader jar for ${task.sourceSetNames.get()}"
        }
    }
}
