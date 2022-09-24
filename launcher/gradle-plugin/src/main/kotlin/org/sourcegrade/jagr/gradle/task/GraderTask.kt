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

package org.sourcegrade.jagr.gradle.task

import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.register
import org.sourcegrade.jagr.gradle.GraderSourceSetConfiguration
import kotlin.reflect.KClass

interface GraderTask : TargetSourceSetTask {

    @get:Input
    val graderName: Property<String>

    interface Factory<T : GraderTask> {
        fun determineTaskName(name: String): String
        fun configureTask(task: T)
    }
}

fun <T : GraderTask> GraderTask.Factory<T>.registerTask(
    project: Project,
    grader: GraderSourceSetConfiguration,
    type: KClass<T>,
): TaskProvider<T> {
    return project.tasks.register(determineTaskName(grader.sourceSet.name), type) { task ->
        task.group = "jagr"
        task.graderName.set(grader.graderName)
        task.sourceSetName.set(grader.sourceSet.name)
        configureTask(task)
    }
}

inline fun <reified T : GraderTask> GraderTask.Factory<T>.registerTask(
    project: Project,
    grader: GraderSourceSetConfiguration,
): TaskProvider<T> {
    return registerTask(project, grader, T::class)
}
