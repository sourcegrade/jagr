package org.jagrkt.api.rubric;

import org.jagrkt.api.testing.TestCycle;
import org.jetbrains.annotations.ApiStatus;

/**
 * Something that can be graded via a {@link TestCycle}
 *
 * @param <G> The resulting graded type
 */
@ApiStatus.NonExtendable
public interface Gradable<G extends Graded> {

  int getMaxPoints();

  int getMinPoints();

  /**
   * Grade the provided {@link TestCycle}
   */
  G grade(TestCycle testCycle);
}
