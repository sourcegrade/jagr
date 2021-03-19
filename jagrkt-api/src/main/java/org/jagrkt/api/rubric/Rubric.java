package org.jagrkt.api.rubric;

import com.google.inject.Inject;
import org.jetbrains.annotations.ApiStatus;

/**
 * A rubric (or template) used for grading an assignment.
 */
@ApiStatus.NonExtendable
public interface Rubric extends Gradable<GradedRubric>, CriterionHolder<Criterion> {

  @ApiStatus.Internal
  class FactoryProvider {
    @Inject
    private static Factory factory;
  }

  static Builder builder() {
    return FactoryProvider.factory.builder();
  }

  /**
   * @return The title of the rubric or exercise, e.g. "H00"
   */
  String getTitle();

  /**
   * The maximum number of points is the sum of the maximum points of the {@link #getChildCriteria() criteria} of this rubric.
   *
   * @return The maximum number of points for this rubric.
   * @see CriterionHolderPointCalculator#maxOfChildren(int)
   */
  int getMaxPoints();

  /**
   * The minimum number of points is the min of the minimum points of the {@link #getChildCriteria() criteria} of this rubric.
   *
   * @return The minimum number of points for this rubric.
   * @see CriterionHolderPointCalculator#minOfChildren(int)
   */
  int getMinPoints();

  @ApiStatus.NonExtendable
  interface Builder {

    Builder title(String title);

    Builder addChildCriteria(Criterion... criterion);

    Builder addChildCriteria(Iterable<? extends Criterion> criterion);

    Rubric build();
  }

  @ApiStatus.Internal
  interface Factory {
    Builder builder();
  }
}
