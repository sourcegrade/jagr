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

package org.sourcegrade.jagr.launcher.executor

import org.sourcegrade.jagr.api.rubric.GradedRubric
import org.sourcegrade.jagr.launcher.io.SerializationScope
import org.sourcegrade.jagr.launcher.io.SerializerFactory
import org.sourcegrade.jagr.launcher.io.get
import org.sourcegrade.jagr.launcher.io.readInstant
import org.sourcegrade.jagr.launcher.io.readMap
import org.sourcegrade.jagr.launcher.io.writeInstant
import org.sourcegrade.jagr.launcher.io.writeMap
import java.time.Instant

data class GradingResult(
    val startedUtc: Instant,
    val finishedUtc: Instant,
    val request: GradingRequest,
    val rubrics: Map<GradedRubric, String>,
) {
    companion object Factory : SerializerFactory<GradingResult> {
        override fun read(scope: SerializationScope.Input) = GradingResult(
            scope.input.readInstant(),
            scope.input.readInstant(),
            scope[GradingRequest::class],
            scope.readMap(),
        )

        override fun write(obj: GradingResult, scope: SerializationScope.Output) {
            scope.output.writeInstant(obj.startedUtc)
            scope.output.writeInstant(obj.finishedUtc)
            // skip request because parent process already has it
            scope.writeMap(obj.rubrics)
        }
    }
}
