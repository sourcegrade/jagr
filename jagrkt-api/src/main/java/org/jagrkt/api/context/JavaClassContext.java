package org.jagrkt.api.context;

import org.checkerframework.checker.nullness.qual.Nullable;

public interface JavaClassContext extends SourceFileContext, JavaContext {

  Class<?> getTargetClass();

  @Nullable JavaClassContext getEnclosingClassContext();
}
