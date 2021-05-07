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
import org.junit.platform.engine.TestExecutionResult
import org.junit.platform.engine.TestExecutionResult.Status.*
import org.junit.platform.engine.TestSource
import org.opentest4j.AssertionFailedError

class TestAwareGraderImpl(
  private val predicate: ((TestCycle, Criterion) -> Boolean)?,
  private val graderPassed: Grader,
  private val graderFailed: Grader,
  private val requirePass: Map<TestSource, String?>,
  private val requireFail: Map<TestSource, String?>,
  private val commentIfFailed: String?
) : Grader {

  override fun grade(testCycle: TestCycle, criterion: Criterion): GradeResult? {
    if (predicate?.invoke(testCycle, criterion) == false) {
      return null
    }
    val statusListener = (testCycle.jUnitResult ?: return null).statusListener
    fun Map<TestSource, String?>.must(predicate: (TestExecutionResult) -> Boolean): GradeResult? {
      val comments: MutableList<String> = mutableListOf()
      var failed = false
      for ((testSource, comment) in this) {
        val testExecutionResult = statusListener[testSource]
        if (testExecutionResult == null || !predicate(testExecutionResult)) {
          failed = true
          // a comment supplied to requirePass or requireFail overrides the default comment from the result's throwable
          (comment ?: testExecutionResult?.message)?.also { comments += it }
        }
      }
      return if (failed) {
        // general comment goes after more specific test comments
        commentIfFailed?.also { comments += it }
        GradeResult.withComments(graderFailed.grade(testCycle, criterion), comments)
      } else null
    }
    requirePass.must { it.status == SUCCESSFUL }?.also { return it }
    requireFail.must { it.status == FAILED }?.also { return it }
    return graderPassed.grade(testCycle, criterion)
  }

  private val TestExecutionResult.message
    get() = throwable.orElse(null)?.run {
      when (this) {
        is AssertionFailedError,
        -> message.toString()
          .replace('>', ']')
          .replace('<', '[')
        is NullPointerException,
        -> "${this::class.simpleName}: $message @ ${stackTrace.firstOrNull()}"
        else -> "${this::class.simpleName}: $message"
      }
    }
}
