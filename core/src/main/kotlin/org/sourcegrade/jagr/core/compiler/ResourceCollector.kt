/*
 *   Jagr - SourceGrade.org
 *   Copyright (C) 2021 Alexander Staeding
 *   Copyright (C) 2021 Contributors
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

package org.sourcegrade.jagr.core.compiler

import org.sourcegrade.jagr.core.testing.GraderInfoImpl
import org.sourcegrade.jagr.core.testing.SubmissionInfoImpl
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KClass

interface ResourceCollector {
  operator fun <T : Any> get(type: KClass<T>): T?
}

inline fun <reified T : Any> ResourceCollector.get() = get(T::class)

val ProcessedContainer.submissionInfo: SubmissionInfoImpl? by collected()

val ProcessedContainer.graderInfo: GraderInfoImpl? by collected()

private inline fun <reified T : Any> collected(): ReadOnlyProperty<ProcessedContainer, T?> =
  ReadOnlyProperty { e, _ -> e.resourceCollector.get() }
