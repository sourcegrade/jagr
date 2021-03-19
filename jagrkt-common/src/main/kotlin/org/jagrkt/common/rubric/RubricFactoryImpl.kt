package org.jagrkt.common.rubric

import org.jagrkt.api.rubric.Rubric

class RubricFactoryImpl : Rubric.Factory {
  override fun builder() = RubricBuilderImpl()
}
