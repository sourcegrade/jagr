package org.jagrkt.common.rubric.grader

import org.jagrkt.api.rubric.Criterion
import org.jagrkt.api.rubric.GradeResult
import org.jagrkt.api.rubric.Grader
import org.jagrkt.api.testing.TestCycle

class DescendingPriorityGrader(
  private vararg val graders: Grader,
) : Grader {

  override fun grade(testCycle: TestCycle, criterion: Criterion): GradeResult? {
    return graders.asSequence().map { it.grade(testCycle, criterion) }.firstOrNull { it != null }
  }
}
