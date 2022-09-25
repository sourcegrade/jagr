package org.sourcegrade.jagr.gradle.task

import org.gradle.api.Project
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.getByType
import org.sourcegrade.jagr.gradle.GraderConfiguration

@Suppress("LeakingThis")
abstract class GraderBuildTask : Jar(), GraderTask {

    @get:InputFile
    val graderInfoFile = project.buildDir.resolve("resources/jagr/grader-info.json")

    init {
        dependsOn(configurationName.map(GraderWriteInfoTask.Factory::determineTaskName))
        archiveFileName.set(graderName.map { "$it-${project.version}.jar" })
        from(graderInfoFile)
        val sourceSets = project.extensions.getByType<SourceSetContainer>()
        from(
            sourceSetNames.zip(dependentConfigurationNames) { a, b -> a + b }
                .map { names -> names.map { name -> sourceSets[name].allSource } }
        )
    }

    object Factory : GraderTask.Factory<GraderBuildTask> {
        override fun determineTaskName(name: String) = "${name}Build"
        override fun configureTask(task: GraderBuildTask, project: Project, grader: GraderConfiguration) {
            task.description = "Builds the grader jar for ${task.sourceSetNames.get()}"
        }
    }
}
