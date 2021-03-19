package org.jagrkt.api.context;

import com.google.inject.Inject;
import org.jagrkt.api.testing.Submission;
import org.jagrkt.api.testing.TestCycle;
import org.jetbrains.annotations.ApiStatus;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

@ApiStatus.NonExtendable
public interface ContextResolver<C extends CodeContext> {

  @ApiStatus.Internal
  class FactoryProvider {
    @Inject
    private static ContextResolver.Factory factory;
  }

  static ContextResolver<? extends CodeContext> ofPath(String path) {
    return FactoryProvider.factory.ofPath(path);
  }

  /**
   * Creates a new {@link CodeContext} that matches the provided filePath. If the file does not exist at runtime,
   * the created instance will show this.
   *
   * @param filePath The path to the file
   * @return A {@link CodeContext} that matches the provided fileName
   */
  static ContextResolver<? extends SourceFileContext> ofSourceFile(String filePath) {
    return FactoryProvider.factory.ofSourceFile(filePath);
  }

  /**
   * Creates a new {@link JavaClassContext JavaClassSelector} that matches the {@link Class}
   * from the provided {@link Callable}. If the class does not exist at runtime, the created instance
   * will show this.
   *
   * @param classSupplier A {@link Callable} which may throw an exception (e.g. {@link ClassNotFoundException}
   * @return A {@link JavaClassContext} that matches the discovered {@link Class}
   */
  static ContextResolver<? extends JavaClassContext> ofJavaClass(Callable<Class<?>> classSupplier) {
    return FactoryProvider.factory.ofJavaClass(classSupplier);
  }

  /**
   * Creates a new {@link JavaMethodContext JavaMethodSelector} that matches the {@link Method}
   * from the provided {@link Callable}. If the method does not exist at runtime, the created instance
   * will show this.
   *
   * @param methodSupplier A {@link Callable} which may throw an exception (e.g. {@link NoSuchMethodException}
   * @return A {@link JavaMethodContext} that matches the discovered {@link Method}
   */
  static ContextResolver<? extends JavaMethodContext> ofJavaMethod(Callable<Method> methodSupplier) {
    return FactoryProvider.factory.ofJavaMethod(methodSupplier);
  }

  /**
   * Resolves a context with the provided {@link TestCycle}. This method produces a new instance on every call.
   */
  C resolve(TestCycle testCycle);

  @ApiStatus.Internal
  interface Factory {
    ContextResolver<? extends PathContext> ofPath(String path);

    ContextResolver<? extends SourceFileContext> ofSourceFile(String filePath);

    ContextResolver<? extends JavaClassContext> ofJavaClass(Callable<Class<?>> classSupplier);

    ContextResolver<? extends JavaMethodContext> ofJavaMethod(Callable<Method> methodSupplier);
  }
}
