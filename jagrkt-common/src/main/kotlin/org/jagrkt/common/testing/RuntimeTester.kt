package org.jagrkt.common.testing

import org.jagrkt.api.testing.Submission
import org.jagrkt.api.testing.TestCycle

fun interface RuntimeTester {
  fun createTestCycle(testJar: TestJar, submission: Submission): TestCycle?
}
