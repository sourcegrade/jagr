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
import org.junit.platform.engine.TestExecutionResult
import org.junit.platform.engine.TestSource
import org.junit.platform.engine.support.descriptor.ClassSource
import org.junit.platform.engine.support.descriptor.MethodSource
import org.junit.platform.launcher.TestIdentifier
import org.opentest4j.AssertionFailedError
import org.slf4j.Logger
import java.lang.reflect.Method
import java.util.concurrent.Callable

class JUnitTestRefFactoryImpl @Inject constructor(
  private val logger: Logger,
) : JUnitTestRef.Factory {

  override fun ofClass(clazz: Class<*>): JUnitTestRef = Default(ClassSource.from(clazz))
  override fun ofMethod(method: Method): JUnitTestRef = Default(MethodSource.from(method))

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

  override fun and(vararg testRefs: JUnitTestRef): JUnitTestRef = And(*testRefs)
  override fun or(vararg testRefs: JUnitTestRef): JUnitTestRef = Or(*testRefs)
  override fun not(testRef: JUnitTestRef): JUnitTestRef = Not(testRef)

  object JUnitNoOpTestRef : JUnitTestRef {
    class NoOpFailedError : AssertionFailedError("Failed to evaluate test")

    override operator fun get(testResults: Map<TestIdentifier, TestExecutionResult>): TestExecutionResult {
      return TestExecutionResult.failed(NoOpFailedError())
    }
  }

  class Default(private val testSource: TestSource) : JUnitTestRef {
    inner class TestNotFoundError : AssertionFailedError("Test $testSource did not run")

    override operator fun get(testResults: Map<TestIdentifier, TestExecutionResult>): TestExecutionResult {
      for ((identifier, result) in testResults) {
        if (testSource == identifier.source.orElse(null)) {
          return result
        }
      }
      return TestExecutionResult.failed(TestNotFoundError())
    }
  }

  class And(private vararg val testRefs: JUnitTestRef) : JUnitTestRef {
    class AndFailedError(cause: Throwable?) : AssertionFailedError("And expression false", cause)

    override operator fun get(testResults: Map<TestIdentifier, TestExecutionResult>): TestExecutionResult {
      for (testRef in testRefs) {
        val result = testRef[testResults]
        if (result.status != TestExecutionResult.Status.SUCCESSFUL) {
          return TestExecutionResult.failed(AndFailedError(result.throwable.orElse(null)))
        }
      }
      return TestExecutionResult.successful()
    }
  }

  class Or(private vararg val testRefs: JUnitTestRef) : JUnitTestRef {
    class OrFailedError : AssertionFailedError("Or expression false")

    override operator fun get(testResults: Map<TestIdentifier, TestExecutionResult>?): TestExecutionResult {
      for (testRef in testRefs) {
        if (testRef[testResults].status == TestExecutionResult.Status.SUCCESSFUL) {
          return testRef[testResults]
        }
      }
      return TestExecutionResult.failed(OrFailedError())
    }
  }

  class Not(private val testRef: JUnitTestRef) : JUnitTestRef {
    class NotFailedError : AssertionFailedError("Not expression false")

    override operator fun get(testResults: Map<TestIdentifier, TestExecutionResult>): TestExecutionResult {
      return if (testRef[testResults].status == TestExecutionResult.Status.SUCCESSFUL) {
        TestExecutionResult.failed(NotFailedError())
      } else {
        TestExecutionResult.successful()
      }
    }
  }
}
