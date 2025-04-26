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

package org.sourcegrade.jagr.launcher.env

fun Config.copyDir(block: Dir.() -> Dir) = copy(dir = block(dir))

fun Config.copyExecutor(block: Executor.() -> Executor) = copy(executor = block(executor))

fun Config.copyExtras(block: Extras.() -> Extras) = copy(extras = block(extras))
fun Extras.copyMoodleUnpack(block: Extras.MoodleUnpack.() -> Extras.MoodleUnpack) = copy(moodleUnpack = block(moodleUnpack))

fun Config.copyTransformers(block: Transformers.() -> Transformers) = copy(transformers = block(transformers))
fun Transformers.copyTimeout(block: Transformers.TimeoutTransformer.() -> Transformers.TimeoutTransformer) = copy(timeout = block(timeout))
