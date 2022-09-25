package org.sourcegrade.jagr.gradle.task

import org.gradle.api.Project
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.getByType
import org.sourcegrade.jagr.gradle.GraderSourceSetConfiguration

@Suppress("LeakingThis")
abstract class GraderBuildTask : Jar(), GraderTask {

    @get:InputFile
    val graderInfoFile = project.buildDir.resolve("resources/jagr/grader-info.json")

    init {
        dependsOn(sourceSetName.map(GraderWriteInfoTask.Factory::determineTaskName))
        archiveFileName.set(graderName.map { "$it-${project.version}.jar" })
        from(graderInfoFile)
        val sourceSets = project.extensions.getByType<SourceSetContainer>()
        from(sourceSets["main"].allSource)
        from(sourceSets["test"].allSource)
        from(sourceSetName.map { sourceSets[it].allSource })
    }

    object Factory : GraderTask.Factory<GraderBuildTask> {
        override fun determineTaskName(name: String) = "${name}Build"
        override fun configureTask(task: GraderBuildTask, project: Project, grader: GraderSourceSetConfiguration) {
            task.description = "Builds the grader jar for ${task.sourceSetName.get()}"
        }
    }
}
