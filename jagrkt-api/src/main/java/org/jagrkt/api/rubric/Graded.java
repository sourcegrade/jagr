package org.jagrkt.api.rubric;

import org.jagrkt.api.testing.TestCycle;
import org.jetbrains.annotations.ApiStatus;

/**
 * Something that has been graded.
 */
@ApiStatus.NonExtendable
public interface Graded {

  TestCycle getTestCycle();

  GradeResult getGrade();
}
