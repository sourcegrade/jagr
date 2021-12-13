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

package org.sourcegrade.jagr.core.rubric.grader

import org.sourcegrade.jagr.api.rubric.Criterion
import org.sourcegrade.jagr.api.rubric.GradeResult
import org.sourcegrade.jagr.api.rubric.Grader
import org.sourcegrade.jagr.api.testing.TestCycle

class ContextAwareGraderImpl(
  private val predicate: ((TestCycle, Criterion) -> Boolean)?,
  private val pointCalculatorPassed: ((TestCycle, Criterion) -> GradeResult)?,
  private val pointCalculatorFailed: ((TestCycle, Criterion) -> GradeResult)?,
) : Grader {

  override fun grade(testCycle: TestCycle, criterion: Criterion): GradeResult? {
    if (predicate?.invoke(testCycle, criterion) == false) {
      return null
    }
    return null // TODO: Finish implementing this
  }
}
