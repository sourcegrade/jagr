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

package org.sourcegrade.jagr.launcher.pipeline

import org.sourcegrade.jagr.api.testing.SubmissionInfo
import org.sourcegrade.jagr.launcher.io.GraderInfo
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KClass

interface ResourceCollector {
    operator fun <T : Any> get(type: KClass<T>): T?
}

inline fun <reified T : Any> ResourceCollector.get() = get(T::class)

val ProcessedContainer.submissionInfo: SubmissionInfo? by collected()

val ProcessedContainer.graderInfo: GraderInfo? by collected()

private inline fun <reified T : Any> collected(): ReadOnlyProperty<ProcessedContainer, T?> =
    ReadOnlyProperty { e, _ -> e.resourceCollector.get() }
