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

import org.jagrkt.api.rubric.JUnitTestRef
import org.junit.platform.engine.TestSource
import org.junit.platform.engine.support.descriptor.ClassSource
import org.junit.platform.engine.support.descriptor.MethodSource
import java.lang.reflect.Method
import java.util.concurrent.Callable

class JUnitTestRefFactoryImpl : JUnitTestRef.Factory {

  override fun ofClass(clazz: Class<*>): JUnitClassTestRef = JUnitClassTestRef(clazz)
  override fun ofMethod(method: Method): JUnitMethodTestRef = JUnitMethodTestRef(method)

  @Throws(IllegalArgumentException::class)
  override fun ofMethod(methodSupplier: Callable<Method>): JUnitMethodTestRef {
    return ofMethod(try {
      methodSupplier.call()
    } catch (e: Throwable) {
      throw IllegalArgumentException("Could not create JUnitTest", e)
    })
  }

  class JUnitClassTestRef(clazz: Class<*>) : JUnitTestRef {
    private val testSource = ClassSource.from(clazz)
    override fun getTestSource(): TestSource = testSource
  }

  class JUnitMethodTestRef(method: Method) : JUnitTestRef {
    private val testSource = MethodSource.from(method)
    override fun getTestSource(): MethodSource = testSource
  }
}
