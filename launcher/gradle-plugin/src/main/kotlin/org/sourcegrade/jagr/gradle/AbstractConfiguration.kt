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

package org.sourcegrade.jagr.gradle

import org.gradle.api.Project
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.kotlin.dsl.getByType

abstract class AbstractConfiguration(
    val name: String,
    private val project: Project,
) {
    private val sourceSetContainer: SourceSetContainer = project.extensions.getByType()
    private val _sourceSets: MutableList<SourceSet> = mutableListOf()
    val sourceSets: List<SourceSet>
        get() = _sourceSets

    abstract val sourceSetNames: ListProperty<String>
    abstract val dependentConfigurations: ListProperty<AbstractConfiguration>

    init {
        project.afterEvaluate {
            for (sourceSetName in sourceSetNames.get()) {
                _sourceSets.add(sourceSetContainer.maybeCreate(sourceSetName))
            }
        }
    }

    fun from(vararg sourceSetNames: String) {
        for (sourceSetName in sourceSetNames) {
            this.sourceSetNames.add(sourceSetName)
        }
    }
}
