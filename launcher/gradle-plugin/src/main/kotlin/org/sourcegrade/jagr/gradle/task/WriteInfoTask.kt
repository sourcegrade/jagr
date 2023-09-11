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

import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.api.internal.GradleInternal
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Input
import org.gradle.kotlin.dsl.listProperty

abstract class WriteInfoTask : DefaultTask() {

    @get:Input
    @Suppress("UnstableApiUsage")
    val repositories: ListProperty<Pair<String, String>> = project.objects.listProperty<Pair<String, String>>().value(
        (project.gradle as GradleInternal).settings.dependencyResolutionManagement.repositories.mapToPairs() +
            project.repositories.mapToPairs(),
    )

    private fun RepositoryHandler.mapToPairs(): List<Pair<String, String>> =
        filterIsInstance<MavenArtifactRepository>().map { it.name to it.url.toString() }
}
