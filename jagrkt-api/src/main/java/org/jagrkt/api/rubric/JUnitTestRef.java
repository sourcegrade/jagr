package org.jagrkt.api.rubric;

import com.google.inject.Inject;
import org.jetbrains.annotations.ApiStatus;
import org.junit.platform.engine.TestSource;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

@FunctionalInterface
public interface JUnitTestRef {

  class FactoryProvider {
    @Inject
    private static Factory factory;
  }

  static JUnitTestRef ofClass(Class<?> clazz) {
    return FactoryProvider.factory.ofClass(clazz);
  }

  static JUnitTestRef ofMethod(Method method) {
    return FactoryProvider.factory.ofMethod(method);
  }

  /**
   * @throws IllegalArgumentException if the method could not be found
   */
  static JUnitTestRef ofMethod(Callable<Method> methodSupplier) {
    return FactoryProvider.factory.ofMethod(methodSupplier);
  }

  TestSource getTestSource();

  @ApiStatus.Internal
  interface Factory {
    JUnitTestRef ofClass(Class<?> clazz);

    JUnitTestRef ofMethod(Method method);

    JUnitTestRef ofMethod(Callable<Method> methodSupplier);
  }
}
