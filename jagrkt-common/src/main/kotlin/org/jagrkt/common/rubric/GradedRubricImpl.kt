package org.jagrkt.common.rubric

import org.jagrkt.api.rubric.GradeResult
import org.jagrkt.api.rubric.GradedCriterion
import org.jagrkt.api.rubric.GradedRubric
import org.jagrkt.api.rubric.Rubric
import org.jagrkt.api.testing.TestCycle

data class GradedRubricImpl(
  private val testCycle: TestCycle,
  private val grade: GradeResult,
  private val rubric: Rubric,
  private val childCriteria: List<GradedCriterion>,
) : GradedRubric {
  override fun getTestCycle(): TestCycle = testCycle
  override fun getGrade(): GradeResult = grade
  override fun getChildCriteria(): List<GradedCriterion> = childCriteria
  override fun getRubric(): Rubric = rubric
}
