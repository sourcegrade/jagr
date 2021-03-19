package org.jagrkt.common.rubric

import org.jagrkt.api.rubric.Criterion
import org.jagrkt.api.rubric.GradeResult

class GradeResultFactoryImpl : GradeResult.Factory {
  private val none = GradeResultImpl(0, 0);
  override fun ofCorrect(points: Int): GradeResult = GradeResultImpl(points, 0)
  override fun ofIncorrect(points: Int): GradeResult = GradeResultImpl(0, points)
  override fun ofNone(): GradeResult = none
  override fun of(correctPoints: Int, incorrectPoints: Int): GradeResult = GradeResultImpl(correctPoints, incorrectPoints)
  override fun of(grade: GradeResult, vararg otherGrades: GradeResult): GradeResult = of(grade, otherGrades.asIterable())

  override fun of(grade: GradeResult, otherGrades: Iterable<GradeResult>): GradeResult {
    var correctPoints = grade.correctPoints
    var incorrectPoints = grade.incorrectPoints
    for (otherGrade in otherGrades) {
      correctPoints += otherGrade.correctPoints
      incorrectPoints += otherGrade.incorrectPoints
    }
    return GradeResultImpl(correctPoints, incorrectPoints)
  }

  override fun ofMax(criterion: Criterion): GradeResult = ofCorrect(criterion.maxPoints)
  override fun ofMin(criterion: Criterion): GradeResult = ofIncorrect(-criterion.minPoints)
}
