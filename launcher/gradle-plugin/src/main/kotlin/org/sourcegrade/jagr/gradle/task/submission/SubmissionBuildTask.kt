package org.sourcegrade.jagr.gradle.task.submission

import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.property
import org.sourcegrade.jagr.gradle.SubmissionConfiguration
import org.sourcegrade.jagr.gradle.task.JagrTaskFactory
import java.io.File

@Suppress("LeakingThis")
abstract class SubmissionBuildTask : Jar(), SubmissionTask {
    @get:InputFile
    val submissionInfoFile: Property<File> = project.objects.property<File>()
        .value(configurationName.map { project.buildDir.resolve("resources/jagr/$it/submission-info.json") })

    init {
        group = "build"
        dependsOn(configurationName.map(SubmissionWriteInfoTask.Factory::determineTaskName))
        from(submissionInfoFile)
        val sourceSets = project.extensions.getByType<SourceSetContainer>()
        from(sourceSetNames.map { names -> names.map { name -> sourceSets[name].allSource } })
        archiveFileName.set(
            assignmentId.zip(lastName) { assignmentId, lastName ->
                "$assignmentId-$lastName"
            }.zip(firstName) { left, firstName ->
                "$left-$firstName-submission"
            }.zip(archiveExtension) { left, extension ->
                "$left.$extension"
            }
        )
    }

    internal object Factory : JagrTaskFactory<SubmissionBuildTask, SubmissionConfiguration> {
        override fun determineTaskName(name: String) = "${name}BuildSubmission"

        override fun configureTask(task: SubmissionBuildTask, project: Project, configuration: SubmissionConfiguration) {
            task.description = "Builds the submission for ${configuration.name}"
        }
    }
}
