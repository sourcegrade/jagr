/*
 *   Jagr - SourceGrade.org
 *   Copyright (C) 2021-2023 Alexander Städing
 *   Copyright (C) 2021-2023 Contributors
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

package org.sourcegrade.jagr.gradle.task.submission

import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.tasks.InputFile
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.property
import org.sourcegrade.jagr.gradle.extension.SubmissionConfiguration
import org.sourcegrade.jagr.gradle.extension.getSourceSet
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

        project.subprojects.forEach { subproject ->
            from(
                sourceSetNames.map { all ->
                    all
                        .filter { it.projectPath == subproject.path }
                        .map { it.getSourceSet(project).allSource }
                },
            ) { copy ->
                copy.into(subproject.path)
            }
        }

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
