package org.jagrkt.common.rubric.grader

import org.jagrkt.api.rubric.Grader

class GraderFactoryImpl : Grader.Factory {
  override fun testAwareBuilder() = TestAwareGraderBuilderImpl()
  override fun descendingPriority(vararg graders: Grader) = DescendingPriorityGrader(*graders)
}
