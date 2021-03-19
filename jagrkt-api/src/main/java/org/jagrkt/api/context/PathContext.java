package org.jagrkt.api.context;

import java.util.List;

public interface PathContext extends CodeContext {

  boolean isRoot();

  List<CodeContext> getChildren();

  /**
   * If {@link #isRoot()} returns {@code this}
   */
  @Override
  PathContext getParent();
}
