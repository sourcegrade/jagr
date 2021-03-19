package org.jagrkt.api.context;

import org.jagrkt.api.testing.TestCycle;
import org.jetbrains.annotations.ApiStatus;

import java.nio.file.Path;

/**
 * Represents a piece of code in a student's solution
 */
@ApiStatus.NonExtendable
public interface CodeContext {

  TestCycle getTestCycle();

  CodeContext getParent();

  /**
   * @return The {@link Path} representing the file location of this context
   * @throws IllegalStateException If not {@link #matched() matched}
   */
  Path getPath();

  /**
   * A context "exists" if it was found in the submission's source code.
   *
   * @return Whether this
   */
  boolean exists();

  /**
   * A context is "matched" if it exists in the solution.
   *
   * <p>
   * The behavior of this instance is defined only if the context is matched. If the context is not matched, methods are not
   * required to complete successfully and may throw an {@link IllegalStateException}.
   * </p>
   *
   * @return Whether this context was successfully matched.
   */
  boolean matched();

  /**
   * A context is "modified" if the modified source code is not equal to the original source code.
   *
   * <p>
   * A precondition to this evaluation is that both the original and modified source code exist. (i.e. This context must
   * {@link #exists() exist} and must be {@link #matched()} for it to be modified)
   * </p>
   *
   * @return Whether this context has been modified
   */
  boolean modified();
}
