package org.jagrkt.common.rubric

import org.jagrkt.api.rubric.Criterion
import org.jagrkt.api.rubric.GradeResult
import org.jagrkt.api.rubric.GradedCriterion
import org.jagrkt.api.testing.TestCycle

data class GradedCriterionImpl(
  private val testCycle: TestCycle,
  private val grade: GradeResult,
  private val criterion: Criterion,
  private val childCriteria: List<GradedCriterion> = listOf(),
) : GradedCriterion {
  override fun getTestCycle(): TestCycle = testCycle
  override fun getGrade(): GradeResult = grade
  override fun getChildCriteria(): List<GradedCriterion> = childCriteria
  override fun getCriterion(): Criterion = criterion
}
