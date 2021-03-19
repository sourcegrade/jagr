package org.jagrkt.api.context;

import java.lang.reflect.Method;

public interface JavaMethodContext extends MethodContext, JavaContext {

  Method getOriginalMethod();

  Method getModifiedMethod();

  @Override
  JavaClassContext getParent();
}
