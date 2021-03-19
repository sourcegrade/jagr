package org.jagrkt.common.rubric

import org.jagrkt.api.rubric.Criterion

class CriterionFactoryImpl : Criterion.Factory {
  override fun builder(): Criterion.Builder = CriterionBuilderImpl()
}
