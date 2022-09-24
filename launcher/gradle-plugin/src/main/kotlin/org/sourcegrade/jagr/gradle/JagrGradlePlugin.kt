package org.sourcegrade.jagr.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import org.sourcegrade.jagr.gradle.task.GraderBuildTask
import org.sourcegrade.jagr.gradle.task.GraderLibsTask
import org.sourcegrade.jagr.gradle.task.GraderRunTask
import org.sourcegrade.jagr.gradle.task.GraderWriteInfoTask
import org.sourcegrade.jagr.gradle.task.registerTask

class JagrGradlePlugin : Plugin<Project> {

    override fun apply(target: Project) {
        // create extensions
        val jagr = target.extensions.create<JagrExtension>("jagr")
        // create tasks
        target.afterEvaluate { registerTasks(jagr, it) }
    }

    private fun registerTasks(jagr: JagrExtension, target: Project) {
        for (grader in jagr.graders) {
            GraderBuildTask.Factory.registerTask(target, grader)
            GraderLibsTask.Factory.registerTask(target, grader)
            GraderWriteInfoTask.Factory.registerTask(target, grader)
            GraderRunTask.Factory.registerTask(target, grader)
        }
    }
}
