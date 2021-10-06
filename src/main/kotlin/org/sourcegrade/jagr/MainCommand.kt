/*
 *   Jagr - SourceGrade.org
 *   Copyright (C) 2021 Alexander Staeding
 *   Copyright (C) 2021 Contributors
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

package org.sourcegrade.jagr

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import org.sourcegrade.jagr.launcher.env.Jagr
import org.sourcegrade.jagr.launcher.executor.Executor
import org.sourcegrade.jagr.launcher.executor.GradingRequest
import org.sourcegrade.jagr.launcher.executor.RubricCollector
import org.sourcegrade.jagr.launcher.executor.SyncExecutor
import org.sourcegrade.jagr.launcher.executor.emptyCollector
import org.sourcegrade.jagr.launcher.executor.gradingQueueOf
import kotlin.coroutines.suspendCoroutine

class MainCommand : CliktCommand() {
  val child by option("--child", "-c").flag()
  override fun run() {

  }

  suspend fun startChildProcess() {
    val executor: Executor = SyncExecutor(Jagr)
    while (true) {
      val nextRequest = nextRequest() ?: break
      executor.schedule(gradingQueueOf(nextRequest))
      val collector = emptyCollector()
      executor.start(collector)
      sendResult(collector)
    }
  }

  private suspend fun nextRequest(): GradingRequest? = suspendCoroutine { continuation ->
    System.`in`.buffered()
  }

  private suspend fun sendResult(collector: RubricCollector): Unit = suspendCoroutine {

  }
}
