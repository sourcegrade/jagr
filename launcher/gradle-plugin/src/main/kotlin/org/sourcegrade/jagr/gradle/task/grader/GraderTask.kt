/*
 *   Jagr - SourceGrade.org
 *   Copyright (C) 2021-2022 Alexander Staeding
 *   Copyright (C) 2021-2022 Contributors
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.sourcegrade.jagr.gradle.task.grader

import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register
import org.sourcegrade.jagr.gradle.GraderConfiguration
import org.sourcegrade.jagr.gradle.JagrExtension
import org.sourcegrade.jagr.gradle.task.JagrTaskFactory
import org.sourcegrade.jagr.gradle.task.TargetAssignmentTask
import org.sourcegrade.jagr.gradle.task.TargetSourceSetsTask
import kotlin.reflect.KClass

interface GraderTask : TargetAssignmentTask, TargetSourceSetsTask {

    @get:Input
    val graderName: Property<String>

    @get:Input
    val submissionConfigurationName: Property<String>

    @get:Input
    val solutionConfigurationName: Property<String>
}

internal fun <T : GraderTask> JagrTaskFactory<T, GraderConfiguration>.registerTask(
    project: Project,
    configuration: GraderConfiguration,
    type: KClass<T>,
): TaskProvider<T> {
    val jagr = project.extensions.getByType<JagrExtension>()
    return project.tasks.register(determineTaskName(configuration.name), type) { task ->
        task.group = "jagr" // TODO: Maybe grader?
        task.assignmentId.set(jagr.assignmentId)
        task.configurationName.set(configuration.name)
        task.graderName.set(configuration.graderName)
        task.sourceSetNames.set(configuration.sourceSetNames)
        task.dependentConfigurationNames.set(configuration.dependentConfigurations.map { c -> c.map { it.name } })
        task.submissionConfigurationName.set(configuration.submissionConfiguration.map { it.name })
        task.solutionConfigurationName.set(configuration.solutionConfiguration.map { it.name })
        configureTask(task, project, configuration)
    }
}

internal inline fun <reified T : GraderTask> JagrTaskFactory<T, GraderConfiguration>.registerTask(
    project: Project,
    configuration: GraderConfiguration,
): TaskProvider<T> = registerTask(project, configuration, T::class)
