package org.jagrkt.api.rubric;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
public interface GradedCriterion extends Graded, CriterionHolder<GradedCriterion> {

  Criterion getCriterion();
}
