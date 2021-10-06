/*
 *   Jagr - SourceGrade.org
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

package org.sourcegrade.jagr.launcher.executor

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.sourcegrade.jagr.launcher.env.Jagr
import org.sourcegrade.jagr.launcher.env.runtimeGrader

class SyncExecutor constructor(
  jagr: Jagr,
) : Executor {

  object Factory : Executor.Factory {
    override fun create(jagr: Jagr): Executor = SyncExecutor(jagr)
  }

  private val mutex = Mutex()
  private val runtimeGrader = jagr.runtimeGrader
  private val scheduled = mutableListOf<GradingQueue>()

  override suspend fun schedule(queue: GradingQueue) = mutex.withLock {
    scheduled += queue
  }

  override suspend fun start(rubricCollector: MutableRubricCollector) = mutex.withLock {
    while (scheduled.isNotEmpty()) {
      val next = scheduled.next() ?: break
      runtimeGrader.grade(rubricCollector.start(next))
    }
    scheduled.clear()
  }
}
