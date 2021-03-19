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
