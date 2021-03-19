package org.jagrkt.common.testing

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

  override fun executionFinished(testIdentifier: TestIdentifier, testExecutionResult: TestExecutionResult) {
    when (testExecutionResult.status) {
      TestExecutionResult.Status.SUCCESSFUL -> testsSucceeded.add(testIdentifier)
      else -> testsFailed.add(testIdentifier)
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
