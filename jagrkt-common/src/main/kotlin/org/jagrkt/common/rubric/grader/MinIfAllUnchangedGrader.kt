package org.jagrkt.common.rubric.grader

import org.jagrkt.api.inspect.ContextResolver
import org.jagrkt.api.rubric.Criterion
import org.jagrkt.api.rubric.GradeResult
import org.jagrkt.api.rubric.Grader
import org.jagrkt.api.testing.TestCycle

class MinIfAllUnchangedGrader(
  private vararg val contexts: ContextResolver<*>
) : Grader {

  override fun grade(testCycle: TestCycle, criterion: Criterion): GradeResult? {
    /*for (context in contexts) {
      if (context.exists() || context.modifiedSource != context.originalSource) {
        return Optional.empty()
      }
    }*/
    return GradeResult.ofMin(criterion)
  }
}
