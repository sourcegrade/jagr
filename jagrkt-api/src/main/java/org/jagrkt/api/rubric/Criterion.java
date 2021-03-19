package org.jagrkt.api.rubric;

import com.google.inject.Inject;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.junit.platform.engine.discovery.DiscoverySelectors;

import java.util.List;

@ApiStatus.NonExtendable
public interface Criterion extends Gradable<GradedCriterion>, CriterionHolder<Criterion> {

  @ApiStatus.Internal
  class FactoryProvider {
    @Inject
    private static Factory factory;
  }

  static Builder builder() {
    return FactoryProvider.factory.builder();
  }

  String getShortDescription();

  /**
   * Hidden notes are not included in the main rubric area and are not meant to be included in the final uploaded rubric.
   */
  @Nullable String getHiddenNotes();

  /**
   * A criterion is terminal iff it does not have any children.
   *
   * @return Whether this criterion is terminal.
   */
  boolean isTerminal();

  /**
   * @return The {@link Rubric} parent. This is not necessarily the direct parent.
   */
  Rubric getParentRubric();

  /**
   * @return The direct parent, this may be a {@link Rubric} or a {@link Criterion}.
   */
  CriterionHolder<? extends Criterion> getParent();

  /**
   * @return The direct {@link Criterion} parent. {@code null} if the parent is not a {@link Criterion}.
   */
  @Nullable Criterion getParentCriterion();

  List<? extends Criterion> getPeers();

  @ApiStatus.NonExtendable
  interface Builder {

    /**
     * This field is required.
     *
     * @param shortDescription The short description
     * @return {@code this}
     */
    Builder shortDescription(String shortDescription);

    Builder hiddenNotes(String hiddenNotes);

    /**
     * Sets the {@link Grader} for this criterion.
     *
     * @param pointCalculator@return {@code this}
     * @see DiscoverySelectors
     */
    Builder grader(@Nullable Grader pointCalculator);

    /**
     * Sets the maximum points for the built {@link Criterion}. Pass {@code null} to
     * {@link #maxPoints(CriterionHolderPointCalculator)} to reset this value to default.
     * <p>
     * Has different default behavior based on whether this criterion is {@link Criterion#isTerminal() terminal} or not.
     * If this criterion is terminal, defaults to 1. Otherwise (i.e. if this criterion has children) defaults to
     * {@link CriterionHolderPointCalculator#maxOfChildren(int)}
     * </p>
     *
     * @param maxPoints The maximum points
     * @return {@code this}
     */
    default Builder maxPoints(int maxPoints) {
      return maxPoints(CriterionHolderPointCalculator.fixed(maxPoints));
    }

    /**
     * Sets the maximum points for this section. Passing {@code null} resets it to default.
     * <p>
     * Has different default behavior based on whether this criterion is {@link Criterion#isTerminal() terminal} or not.
     * If this criterion is terminal, defaults to 1. Otherwise (i.e. if this criterion has children) defaults to
     * {@link CriterionHolderPointCalculator#maxOfChildren(int)}
     * </p>
     *
     * @param maxPointsCalculator The {@link CriterionHolderPointCalculator}
     * @return {@code this}
     */
    Builder maxPoints(@Nullable CriterionHolderPointCalculator maxPointsCalculator);

    /**
     * Sets the minimum points for the built {@link Criterion}. Pass {@code null} to
     * {@link #minPoints(CriterionHolderPointCalculator)} to reset this value to default.
     * <p>
     * Has different default behavior based on whether this criterion is {@link Criterion#isTerminal() terminal} or not.
     * If this criterion is terminal, defaults to 0. Otherwise (i.e. if this criterion has children) defaults to
     * {@link CriterionHolderPointCalculator#minOfChildren(int)}
     * </p>
     *
     * @param minPoints The minimum points
     * @return {@code this}
     */
    default Builder minPoints(int minPoints) {
      return minPoints(CriterionHolderPointCalculator.fixed(minPoints));
    }

    /**
     * Sets the minimum points for this section. Passing {@code null} resets this value to default.
     * <p>
     * Has different default behavior based on whether this criterion is {@link Criterion#isTerminal() terminal} or not.
     * If this criterion is terminal, defaults to 0. Otherwise (i.e. if this criterion has children) defaults to
     * {@link CriterionHolderPointCalculator#minOfChildren(int)}
     * </p>
     *
     * @param minPointsCalculator The {@link CriterionHolderPointCalculator}
     * @return {@code this}
     */
    Builder minPoints(@Nullable CriterionHolderPointCalculator minPointsCalculator);

    Builder addChildCriteria(Criterion... criteria);

    Criterion build();
  }

  @ApiStatus.Internal
  interface Factory {
    Builder builder();
  }
}
