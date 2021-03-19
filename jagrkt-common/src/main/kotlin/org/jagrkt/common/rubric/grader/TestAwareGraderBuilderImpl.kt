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

import org.jagrkt.api.rubric.Grader
import org.jagrkt.api.rubric.JUnitTestRef

class TestAwareGraderBuilderImpl : AbstractGraderBuilder<Grader.TestAwareBuilder>(), Grader.TestAwareBuilder {

  override fun getThis(): Grader.TestAwareBuilder = this

  private val requirePass: MutableList<JUnitTestRef> = mutableListOf()
  private val requireFail: MutableList<JUnitTestRef> = mutableListOf()

  override fun requirePass(vararg testRefs: JUnitTestRef): Grader.TestAwareBuilder {
    requirePass.addAll(testRefs)
    return this
  }

  override fun requireFail(vararg testRef: JUnitTestRef): Grader.TestAwareBuilder {
    requireFail.addAll(testRef)
    return this
  }

  override fun build(): Grader {
    return TestAwareGraderImpl(
      predicate,
      pointCalculatorPassed,
      pointCalculatorFailed,
      requirePass.map { it.testSource },
      requireFail.map { it.testSource },
    )
  }
}
