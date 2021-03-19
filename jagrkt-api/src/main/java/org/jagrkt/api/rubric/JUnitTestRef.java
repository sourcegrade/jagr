/*
 *   JagrKt - JagrKt.org
 *   Copyright (C) 2021 Alexander Staeding
 *   Copyright (C) 2021 Contributors
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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
