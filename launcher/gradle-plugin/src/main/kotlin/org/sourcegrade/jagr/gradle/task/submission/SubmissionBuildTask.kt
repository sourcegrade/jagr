package org.sourcegrade.jagr.gradle.task.submission

import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.jvm.tasks.Jar
import org.sourcegrade.jagr.gradle.extension.SubmissionConfiguration
import org.sourcegrade.jagr.gradle.extension.createSubmissionInfoFileProperty
import org.sourcegrade.jagr.gradle.extension.getSourceSet
import org.sourcegrade.jagr.gradle.task.JagrTaskFactory

@Suppress("LeakingThis")
abstract class SubmissionBuildTask : Jar(), SubmissionTask {

    @get:InputFile
    val submissionInfoFile: RegularFileProperty = createSubmissionInfoFileProperty(configurationName)

    init {
        group = "build"
        dependsOn(configurationName.map(SubmissionWriteInfoTask.Factory::determineTaskName))
        from(submissionInfoFile)
        from(sourceSetNames.map { all -> all.map { it.getSourceSet(project).allSource } })
        archiveFileName.set(
            assignmentId.zip(studentId) { assignmentId, studentId ->
                "$assignmentId-$studentId"
            }.zip(firstName) { left, firstName ->
                "$left-$firstName"
            }.zip(lastName) { left, lastName ->
                "$left-$lastName-submission"
            }.zip(archiveExtension) { left, extension ->
                "$left.$extension"
            },
        )
    }

    internal object Factory : JagrTaskFactory<SubmissionBuildTask, SubmissionConfiguration> {
        override fun determineTaskName(name: String) = "${name}BuildSubmission"

        override fun configureTask(task: SubmissionBuildTask, project: Project, configuration: SubmissionConfiguration) {
            task.description = "Builds the submission for ${configuration.name}"
        }
    }
}
