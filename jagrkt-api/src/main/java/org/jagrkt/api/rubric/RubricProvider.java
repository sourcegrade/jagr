package org.jagrkt.api.rubric;

import org.jagrkt.api.testing.Submission;
import org.jetbrains.annotations.Nullable;

public interface RubricProvider {

  /**
   * The filename to use when writing this rubric to disk. By default, it is:
   *
   * <pre><code>
   * assignmentId + "_Rubric_" + studentLastName + "_" + studentFirstName
   * </code></pre>
   * <p>
   * If there are multiple rubrics with conflicting names, a number will be added on the end
   */
  default @Nullable String getOutputFileName(Submission submission) {
    return null;
  }

  Rubric getRubric();
}
