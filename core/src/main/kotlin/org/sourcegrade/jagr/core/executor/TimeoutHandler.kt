/*
 *   Jagr - SourceGrade.org
 *   Copyright (C) 2021-2022 Alexander Staeding
 *   Copyright (C) 2021-2022 Contributors
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.sourcegrade.jagr.core.executor

import com.google.inject.Inject
import org.apache.logging.log4j.Logger
import org.opentest4j.AssertionFailedError
import org.sourcegrade.jagr.api.testing.TestCycle
import org.sourcegrade.jagr.core.executor.TimeoutHandler.checkTimeout
import org.sourcegrade.jagr.core.transformer.CommonClassTransformer
import org.sourcegrade.jagr.launcher.env.Config
import java.lang.management.ManagementFactory
import java.util.Arrays
import java.util.concurrent.atomic.AtomicLong

/**
 * Injected via ASM by [CommonClassTransformer].
 *
 * @see [checkTimeout]
 */
object TimeoutHandler {

    @Inject
    @JvmStatic
    private lateinit var config: Config

    @Inject
    @JvmStatic
    private lateinit var logger: Logger

    private val timeoutIndividual by lazy { config.executor.timeoutIndividual }
    private val timeoutTotal by lazy { config.executor.timeoutTotal }

    private val lastTimeout = ThreadLocal.withInitial { AtomicLong(-1) }
    private val testClassNames = ThreadLocal.withInitial<Set<String>> { emptySet() }
    private val testCycle = ThreadLocal<TestCycle>()
    private val mxBean = ManagementFactory.getThreadMXBean()

    internal fun disableTimeout() = lastTimeout.get().set(-1)
    internal fun resetTimeout() = lastTimeout.get().set(0)
    internal fun initialize(testClassNames: Set<String>, testCycle: TestCycle) {
        this.testClassNames.set(testClassNames)
        this.testCycle.set(testCycle)
    }

    /**
     * Checks whether the current [Thread] has reached a timeout, throwing an [AssertionFailedError] if so.
     *
     * @throws AssertionFailedError If the timeout has been reached.
     */
    @JvmStatic
    fun checkTimeout() {
        val lastTimeout = lastTimeout.get().get()
        if (lastTimeout == -1L) {
            // do nothing
            return
        }
        val currentThread = Thread.currentThread()
        val userTime = mxBean.getThreadUserTime(currentThread.id) / 1_000_000L
        if (lastTimeout == 0L) {
            this.lastTimeout.get().set(userTime)
        } else if (userTime > timeoutTotal) {
            val timeoutLocation = getTimeoutLocation()
            logger.error("Total timeout after " + timeoutTotal + "ms @ " + currentThread.name, timeoutLocation)
            throw AssertionFailedError("Total timeout after " + timeoutTotal + "ms")
        } else if (userTime - lastTimeout > timeoutIndividual) {
            val timeoutLocation = getTimeoutLocation()
            logger.error("Timeout after " + timeoutIndividual + "ms @ " + currentThread.name, timeoutLocation)
            // reset LAST_TIMEOUT so that the next checkTimeout() invocation doesn't immediately fail
            this.lastTimeout.get().set(userTime)
            throw AssertionFailedError("Timeout after " + timeoutIndividual + "ms")
        }
    }

    /**
     * @return The String representing the StackElement of the Test, that produced the timeout
     */
    private fun getTimeoutLocation(): Throwable {
        val testCycle: TestCycle? = testCycle.get()
        if (testCycle != null) {
            val testClassNames: Set<String> = testCycle.classLoader.classNames
            val timeoutLocator = Exception("Submission ${testCycle.submission.info}")
            val trace = timeoutLocator.stackTrace
            var i = 1
            for (element in trace) {
                if (element.className in testClassNames) {
                    timeoutLocator.stackTrace = Arrays.copyOfRange(trace, 2, i)
                    return timeoutLocator
                }
                i++
            }
        }
        return Exception("Unable to calculate timeout location")
    }
}
