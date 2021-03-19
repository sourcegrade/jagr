package org.jagrkt.common.rubric

import com.google.common.base.MoreObjects
import org.jagrkt.api.rubric.CriterionHolderPointCalculator
import org.jagrkt.api.rubric.GradeResult
import org.jagrkt.api.rubric.Graded
import org.jagrkt.api.rubric.GradedRubric
import org.jagrkt.api.rubric.Rubric
import org.jagrkt.api.testing.TestCycle

class RubricImpl(
  private val title: String,
  private val criteria: List<CriterionImpl>,
) : Rubric {

  init {
    for (criterion in criteria) {
      criterion.setParent(this)
    }
  }

  private val maxPointsKt: Int by lazy { CriterionHolderPointCalculator.maxOfChildren(0).getPoints(this) }
  private val minPointsKt: Int by lazy { CriterionHolderPointCalculator.minOfChildren(0).getPoints(this) }

  override fun getTitle(): String = title
  override fun getMaxPoints(): Int = maxPointsKt
  override fun getMinPoints(): Int = minPointsKt
  override fun getChildCriteria(): List<CriterionImpl> = criteria

  override fun grade(testCycle: TestCycle): GradedRubric {
    val childGraded = childCriteria.map { it.grade(testCycle) }
    val gradeResult = GradeResult.of(GradeResult.ofNone(), childGraded.map(
      Graded::getGrade))
    return GradedRubricImpl(testCycle, gradeResult, this, childGraded)
  }

  private val stringRep: String by lazy {
    MoreObjects.toStringHelper(this)
      .add("title", title)
      .add("maxPoints", maxPointsKt)
      .add("minPoints", minPointsKt)
      .add("childCriteria", "[" + criteria.joinToString(", ") + "]")
      .toString()
  }

  override fun toString(): String = stringRep
}
