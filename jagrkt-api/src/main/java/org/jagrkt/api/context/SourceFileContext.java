package org.jagrkt.api.context;

import org.jagrkt.api.testing.SourceFile;

public interface SourceFileContext extends CodeContext {

  SourceFile getModifiedSourceFile();

  SourceFile getOriginalSourceFile();

  String getModifiedSource();

  String getOriginalSource();

  /**
   * @return The first line of this context in the source file as a 1-based line number
   */
  int getLineNumber();
}
