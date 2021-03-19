package org.jagrkt.api.rubric;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
public interface GradedRubric extends Graded, CriterionHolder<GradedCriterion> {

  Rubric getRubric();
}
