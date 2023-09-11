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

package org.sourcegrade.jagr.launcher.executor

import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume

/**
 * Synchronization helper class for [MultiWorkerExecutor].
 *
 * Resumes [continuation] when it receives a notification through [notifyContinuation] (e.g. a [Worker] is finished).
 */
internal class WorkerSynchronizer {
    private val finishedBetweenContinuation = AtomicBoolean()
    private val exitDirectly = AtomicBoolean()
    private val lock = ReentrantLock()
    private var continuation: Continuation<Unit>? = null

    fun setContinuation(continuation: Continuation<Unit>, exitDirectly: Boolean = false) = lock.withLock {
        this.continuation = continuation
        this.exitDirectly.set(exitDirectly)
    }

    fun notifyContinuation() = lock.withLock {
        if (finishedBetweenContinuation.get()) {
            if (exitDirectly.get()) {
                resume()
            } else {
                return
            }
        }
        val cont = continuation
        if (cont == null) {
            finishedBetweenContinuation.set(true)
        } else {
            resume()
        }
    }

    fun handleBetween() = lock.withLock {
        if (finishedBetweenContinuation.get()) {
            finishedBetweenContinuation.set(false)
            resume()
        }
    }

    private fun resume() {
        continuation?.resume(Unit)
        continuation = null
    }
}
