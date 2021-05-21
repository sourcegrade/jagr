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

package org.jagrkt.common.rubric

import com.google.inject.Inject
import org.jagrkt.api.rubric.JUnitTestRef
import org.junit.platform.engine.TestSource
import org.junit.platform.engine.support.descriptor.ClassSource
import org.junit.platform.engine.support.descriptor.MethodSource
import org.slf4j.Logger
import java.lang.reflect.Method
import java.util.concurrent.Callable

class JUnitTestRefFactoryImpl @Inject constructor(
  private val logger: Logger,
) : JUnitTestRef.Factory {

  override fun ofClass(clazz: Class<*>): JUnitTestRef = JUnitClassTestRef(clazz)
  override fun ofMethod(method: Method): JUnitTestRef = JUnitMethodTestRef(method)

  override fun ofClass(clazzSupplier: Callable<Class<*>>): JUnitTestRef {
    return try {
      ofClass(clazzSupplier.call())
    } catch (e: Throwable) {
      logger.error("Could not create JUnitClassTestRef :: ${e::class.simpleName}: ${e.message}")
      JUnitNoOpTestRef
    }
  }

  override fun ofMethod(methodSupplier: Callable<Method>): JUnitTestRef {
    return try {
      ofMethod(methodSupplier.call())
    } catch (e: Throwable) {
      logger.error("Could not create JUnitMethodTestRef :: ${e::class.simpleName}: ${e.message}")
      JUnitNoOpTestRef
    }
  }

  object JUnitNoOpTestRef : JUnitTestRef {
    object NoOpTestSource : TestSource
    override fun getTestSource(): NoOpTestSource = NoOpTestSource
  }

  class JUnitClassTestRef(clazz: Class<*>) : JUnitTestRef {
    private val testSource = ClassSource.from(clazz)
    override fun getTestSource(): ClassSource = testSource
  }

  class JUnitMethodTestRef(method: Method) : JUnitTestRef {
    private val testSource = MethodSource.from(method)
    override fun getTestSource(): MethodSource = testSource
  }
}
