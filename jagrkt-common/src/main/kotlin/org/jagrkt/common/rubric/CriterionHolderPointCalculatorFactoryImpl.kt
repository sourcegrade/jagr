package org.jagrkt.common.rubric

import org.jagrkt.api.rubric.Criterion
import org.jagrkt.api.rubric.CriterionHolderPointCalculator

class CriterionHolderPointCalculatorFactoryImpl : CriterionHolderPointCalculator.Factory {
  override fun fixed(points: Int): CriterionHolderPointCalculator =
    CriterionHolderPointCalculator { points }

  override fun maxOfChildren(defaultPoints: Int): CriterionHolderPointCalculator {
    return CriterionHolderPointCalculator {
      it.childCriteria.asSequence().map(Criterion::getMaxPoints)
        .ifEmpty { listOf(defaultPoints).asSequence() }.sum()
    }
  }

  override fun minOfChildren(defaultPoints: Int): CriterionHolderPointCalculator {
    return CriterionHolderPointCalculator {
      it.childCriteria.asSequence().map(Criterion::getMinPoints)
        .ifEmpty { listOf(defaultPoints).asSequence() }.sum()
    }
  }
}
