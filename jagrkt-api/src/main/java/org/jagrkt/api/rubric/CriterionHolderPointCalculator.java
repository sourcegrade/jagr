package org.jagrkt.api.rubric;

import com.google.inject.Inject;
import org.jetbrains.annotations.ApiStatus;

/**
 * A functional interface that calculates a number based on the children of a {@link CriterionHolder}
 */
@FunctionalInterface
public interface CriterionHolderPointCalculator {

  @ApiStatus.Internal
  class FactoryProvider {
    @Inject
    private static Factory factory;
  }

  /**
   * @param points The points that the created calculator should return
   * @return A calculator that always returns the provided {@code points}
   */
  static CriterionHolderPointCalculator fixed(int points) {
    return FactoryProvider.factory.fixed(points);
  }

  /**
   * @return A calculator that returns the sum of the maximum points of the children of the provided {@link Criterion}
   */
  static CriterionHolderPointCalculator maxOfChildren(int defaultPoints) {
    return FactoryProvider.factory.maxOfChildren(defaultPoints);
  }

  /**
   * @return A calculator that returns the sum of the minimum points of the children of the provided {@link Criterion}
   */
  static CriterionHolderPointCalculator minOfChildren(int defaultPoints) {
    return FactoryProvider.factory.minOfChildren(defaultPoints);
  }

  int getPoints(CriterionHolder<Criterion> criterionHolder);

  @ApiStatus.Internal
  interface Factory {
    CriterionHolderPointCalculator fixed(int points);

    CriterionHolderPointCalculator maxOfChildren(int defaultPoints);

    CriterionHolderPointCalculator minOfChildren(int defaultPoints);
  }
}
