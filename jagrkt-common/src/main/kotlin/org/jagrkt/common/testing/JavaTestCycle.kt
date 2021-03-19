package org.jagrkt.common.testing

import org.jagrkt.api.testing.TestCycle

data class JavaTestCycle(
  private val rubricProviderClassNames: List<String>,
  private val submission: JavaSubmission,
  private val classLoader: ClassLoader,
  private val junitResult: TestCycle.JUnitResult?,
) : TestCycle {

  override fun getRubricProviderClassNames(): List<String> = rubricProviderClassNames
  override fun getClassLoader(): ClassLoader = classLoader
  override fun getSubmission(): JavaSubmission = submission
  override fun getJUnitResult(): TestCycle.JUnitResult? = junitResult
}
