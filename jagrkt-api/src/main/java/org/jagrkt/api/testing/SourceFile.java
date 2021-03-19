package org.jagrkt.api.testing;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

@ApiStatus.NonExtendable
public interface SourceFile {

  String getFileName();

  String getContent();

  @Nullable String getClassName();
}
