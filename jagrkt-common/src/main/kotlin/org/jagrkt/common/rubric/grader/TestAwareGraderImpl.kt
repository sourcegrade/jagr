/*
 *   JagrKt - JagrKt.org
 *   Copyright (C) 2021 Alexander Staeding
 *   Copyright (C) 2021 Contributors
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.jagrkt.common.rubric.grader

import org.jagrkt.api.rubric.Criterion
import org.jagrkt.api.rubric.GradeResult
import org.jagrkt.api.rubric.Grader
import org.jagrkt.api.testing.TestCycle
import org.junit.platform.engine.TestSource

class TestAwareGraderImpl(
  private val predicate: ((TestCycle, Criterion) -> Boolean)?,
  private val pointCalculatorPassed: ((TestCycle, Criterion) -> GradeResult)?,
  private val pointCalculatorFailed: ((TestCycle, Criterion) -> GradeResult)?,
  private val requirePass: List<TestSource>,
  private val requireFail: List<TestSource>,
) : Grader {

  override fun grade(testCycle: TestCycle, criterion: Criterion): GradeResult? {
    if (predicate?.invoke(testCycle, criterion) == false) {
      return null
    }
    val statusListener = (testCycle.jUnitResult ?: return null).statusListener
    for (test in requirePass) {
      if (!statusListener.succeeded(test)) {
        return pointCalculatorFailed?.invoke(testCycle, criterion)
      }
    }
    for (test in requireFail) {
      if (!statusListener.failed(test)) {
        return pointCalculatorFailed?.invoke(testCycle, criterion)
      }
    }
    return pointCalculatorPassed?.invoke(testCycle, criterion)
  }
}
