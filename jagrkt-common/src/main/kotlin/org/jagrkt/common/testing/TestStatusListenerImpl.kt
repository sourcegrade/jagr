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

package org.jagrkt.common.testing

import org.jagrkt.api.testing.SubmissionInfo
import org.jagrkt.api.testing.TestStatusListener
import org.junit.platform.engine.TestExecutionResult
import org.junit.platform.engine.TestSource
import org.junit.platform.engine.support.descriptor.ClassSource
import org.junit.platform.engine.support.descriptor.MethodSource
import org.junit.platform.launcher.TestExecutionListener
import org.junit.platform.launcher.TestIdentifier
import org.slf4j.Logger

class TestStatusListenerImpl(
  private val logger: Logger,
) : TestExecutionListener, TestStatusListener {

  private val testsSucceeded: MutableList<TestIdentifier> = mutableListOf()
  private val testsFailed: MutableList<TestIdentifier> = mutableListOf()
  private val linkageErrors: MutableSet<Pair<String?, String>> = LinkedHashSet()

  override fun executionFinished(testIdentifier: TestIdentifier, testExecutionResult: TestExecutionResult) {
    when (testExecutionResult.status) {
      TestExecutionResult.Status.SUCCESSFUL -> testsSucceeded.add(testIdentifier)
      else -> testsFailed.add(testIdentifier)
    }
    testExecutionResult.throwable.orElse(null)?.also { throwable ->
      if (throwable is LinkageError) {
        linkageErrors.add(throwable::class.simpleName to throwable.message + " @ " + throwable.stackTrace.firstOrNull())
      }
    }
  }

  fun logLinkageErrors(info: SubmissionInfo) {
    for (errorMessage in linkageErrors) {
      logger.error("Linkage error @ $info :: $errorMessage")
    }
  }

  override fun succeeded(testSource: TestSource): Boolean = testsSucceeded.hasMatch(testSource)

  override fun failed(testSource: TestSource): Boolean = testsFailed.hasMatch(testSource)

  private fun MutableList<TestIdentifier>.hasMatch(testSource: TestSource): Boolean {
    for (test in this) {
      if (when (val source = test.source.orElse(null) ?: continue) {
          is ClassSource -> source.matches(testSource)
          is MethodSource -> source == testSource
          else -> false
        }
      ) return true
    }
    return false
  }

  private fun ClassSource.matches(other: TestSource): Boolean {
    return when {
      other !is ClassSource -> false
      !other.position.isPresent -> className == other.className
      else -> this == other
    }
  }
}
