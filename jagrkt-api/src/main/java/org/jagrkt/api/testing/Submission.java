package org.jagrkt.api.testing;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

@ApiStatus.NonExtendable
public interface Submission {

  SubmissionInfo getInfo();

  @Nullable SourceFile getSourceFile(String fileName);
}
