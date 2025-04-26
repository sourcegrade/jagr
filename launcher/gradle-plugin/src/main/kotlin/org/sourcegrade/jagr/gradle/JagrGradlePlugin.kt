package org.sourcegrade.jagr.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.register
import org.sourcegrade.jagr.gradle.extension.JagrDownloadExtension
import org.sourcegrade.jagr.gradle.extension.JagrExtension
import org.sourcegrade.jagr.gradle.task.JagrDownloadTask
import org.sourcegrade.jagr.gradle.task.grader.GraderBuildTask
import org.sourcegrade.jagr.gradle.task.grader.GraderLibsTask
import org.sourcegrade.jagr.gradle.task.grader.GraderRunTask
import org.sourcegrade.jagr.gradle.task.grader.GraderWriteInfoTask
import org.sourcegrade.jagr.gradle.task.grader.registerTask
import org.sourcegrade.jagr.gradle.task.submission.SubmissionBuildTask
import org.sourcegrade.jagr.gradle.task.submission.SubmissionWriteInfoTask
import org.sourcegrade.jagr.gradle.task.submission.registerTask

@Suppress("unused")
class JagrGradlePlugin : Plugin<Project> {

    override fun apply(target: Project) {
        // create extensions
        val jagr = target.extensions.create<JagrExtension>("jagr")
        val jagrDownload = jagr.extensions.create<JagrDownloadExtension>("download")
        // register tasks
        target.tasks.register<JagrDownloadTask>("jagrDownload") {
            sourceUrl.set(jagrDownload.sourceUrl)
            destName.set(jagrDownload.destName)
        }
        target.afterEvaluate { registerTasks(jagr, it) }
    }

    private fun registerTasks(jagr: JagrExtension, target: Project) {
        val checkTask = target.tasks["check"]
        for (grader in jagr.graders) {
            GraderBuildTask.Factory.registerTask(target, grader)
            GraderLibsTask.Factory.registerTask(target, grader)
            GraderWriteInfoTask.Factory.registerTask(target, grader)
            GraderRunTask.Factory.registerTask(target, grader)
            checkTask.dependsOn(GraderRunTask.Factory.determineTaskName(grader.name))
        }
        for (submission in jagr.submissions) {
            SubmissionBuildTask.Factory.registerTask(target, submission)
            SubmissionWriteInfoTask.Factory.registerTask(target, submission)
        }
    }
}
