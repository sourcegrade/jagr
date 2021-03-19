package org.jagrkt.api.testing;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.junit.platform.launcher.TestPlan;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;

import java.util.List;

@ApiStatus.NonExtendable
public interface TestCycle {

  List<String> getRubricProviderClassNames();

  /**
   * Every test cycle uses a unique {@link ClassLoader} that loads the grader jar's classes and the {@link Submission}'s
   * classes if it is a java submission.
   *
   * @return The {@link ClassLoader} used in this test cycle
   */
  ClassLoader getClassLoader();

  Submission getSubmission();

  @Nullable JUnitResult getJUnitResult();

  @ApiStatus.NonExtendable
  interface JUnitResult {
    TestPlan getTestPlan();

    SummaryGeneratingListener getSummaryListener();

    TestStatusListener getStatusListener();
  }
}
