package org.jagrkt.common.testing

import org.jagrkt.api.testing.TestCycle
import org.jagrkt.api.testing.TestStatusListener
import org.junit.platform.launcher.TestPlan
import org.junit.platform.launcher.listeners.SummaryGeneratingListener

data class JUnitResultImpl(
  private val testPlan: TestPlan,
  private val summaryListener: SummaryGeneratingListener,
  private val statusListener: TestStatusListener,
) : TestCycle.JUnitResult {
  override fun getTestPlan(): TestPlan = testPlan
  override fun getSummaryListener(): SummaryGeneratingListener = summaryListener
  override fun getStatusListener(): TestStatusListener = statusListener
}
