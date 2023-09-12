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

package org.sourcegrade.jagr.gradle.extension

import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.property

abstract class SubmissionConfiguration(
    name: String,
    project: Project,
) : AbstractConfiguration(name, project, setOf("main", "test")) {
    abstract val studentId: Property<String>
    abstract val firstName: Property<String>
    abstract val lastName: Property<String>
    val checkCompilation: Property<Boolean> = project.objects.property<Boolean>().convention(true)

    fun skipCompilationCheck() {
        checkCompilation.set(false)
    }
}
