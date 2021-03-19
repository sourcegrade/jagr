package org.jagrkt.api.context;

public interface JavaContext extends SourceFileContext {

  ClassLoader getSolutionClassLoader();

  ClassLoader getClassLoader();

  SourceFileContext getFileContext();
}
