package org.sourcegrade.jagr.gradle.task

import org.gradle.api.Project
import org.gradle.api.Task
import org.sourcegrade.jagr.gradle.extension.AbstractConfiguration

internal interface JagrTaskFactory<T : Task, C : AbstractConfiguration> {
    fun determineTaskName(name: String): String
    fun configureTask(task: T, project: Project, configuration: C)
}
