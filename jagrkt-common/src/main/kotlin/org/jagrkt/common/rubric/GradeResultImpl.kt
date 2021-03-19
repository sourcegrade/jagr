package org.jagrkt.common.rubric

import org.jagrkt.api.rubric.GradeResult

data class GradeResultImpl(
  private val correctPoints: Int,
  private val incorrectPoints: Int,
) : GradeResult {
  override fun getCorrectPoints(): Int = correctPoints
  override fun getIncorrectPoints(): Int = incorrectPoints
}
