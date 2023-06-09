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

package org.sourcegrade.jagr.gradle.extension

import org.gradle.api.Project
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.kotlin.dsl.getByType
import java.io.Serializable

data class ProjectSourceSetTuple(
    val projectPath: String,
    val sourceSetName: String,
) : Serializable {
    companion object {
        fun fromSourceSetNames(projectPath: String, sourceSetNames: Sequence<String>): Set<ProjectSourceSetTuple> =
            sourceSetNames.map { ProjectSourceSetTuple(projectPath, it) }.toSet()

        fun fromSourceSetNames(projectPath: String, sourceSetNames: Iterable<String>): Set<ProjectSourceSetTuple> =
            fromSourceSetNames(projectPath, sourceSetNames.asSequence())
    }
}

fun ProjectSourceSetTuple.getSourceSet(rootProject: Project): SourceSet =
    rootProject.relative(projectPath).extensions.getByType<SourceSetContainer>().getByName(sourceSetName)
