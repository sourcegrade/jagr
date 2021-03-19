package org.jagrkt.common.rubric

import org.jagrkt.api.rubric.Criterion
import org.jagrkt.api.rubric.Rubric

class RubricBuilderImpl : Rubric.Builder {

  private var title: String? = null
  private val criteria: MutableList<CriterionImpl> = mutableListOf()

  override fun title(title: String): RubricBuilderImpl {
    this.title = title
    return this
  }

  override fun addChildCriteria(vararg criteria: Criterion): RubricBuilderImpl {
    for (criterion in criteria) {
      this.criteria.add(criterion as CriterionImpl)
    }
    return this
  }

  override fun addChildCriteria(criteria: Iterable<Criterion>): RubricBuilderImpl {
    for (criterion in criteria) {
      this.criteria.add(criterion as CriterionImpl)
    }
    return this
  }

  override fun build(): RubricImpl {
    return RubricImpl(
      requireNotNull(title) { "title is null" },
      criteria,
    )
  }
}
