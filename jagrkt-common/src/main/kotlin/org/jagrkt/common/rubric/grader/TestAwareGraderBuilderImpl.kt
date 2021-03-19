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
