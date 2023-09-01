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

@file:Suppress("UnstableApiUsage")

package org.sourcegrade.jagr

import com.google.common.io.ByteStreams
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.sourcegrade.jagr.launcher.env.Environment
import org.sourcegrade.jagr.launcher.env.Jagr
import org.sourcegrade.jagr.launcher.env.logger
import org.sourcegrade.jagr.launcher.executor.GradingQueue
import org.sourcegrade.jagr.launcher.executor.GradingRequest
import org.sourcegrade.jagr.launcher.executor.GradingResult
import org.sourcegrade.jagr.launcher.executor.ProcessWorker
import org.sourcegrade.jagr.launcher.executor.SyncExecutor
import org.sourcegrade.jagr.launcher.executor.emptyCollector
import org.sourcegrade.jagr.launcher.executor.toGradingQueue
import org.sourcegrade.jagr.launcher.io.SerializerFactory
import org.sourcegrade.jagr.launcher.io.get
import org.sourcegrade.jagr.launcher.io.getScoped
import org.sourcegrade.jagr.launcher.io.openScope
import java.io.ByteArrayOutputStream

class ChildProcGrading(private val jagr: Jagr = Jagr) {
    fun grade() = runBlocking {
        val queue = runCatching { next() }.getOrElse {
            jagr.logger.error("Could not get next GradingQueue", it)
            return@runBlocking
        }
        val collector = emptyCollector(jagr)
        collector.allocate(queue)
        val executor = SyncExecutor(jagr)
        executor.schedule(queue)
        executor.start(collector)
        collector.withGradingFinished { it.firstOrNull()?.sendToParent() }
        Environment.stdOut.close()
    }

    private suspend fun next(): GradingQueue = withContext(Dispatchers.IO) {
        val bytes: ByteArray = runCatching { System.`in`.readAllBytes() }.getOrElse {
            throw IllegalStateException("Encountered an unrecoverable exception receiving bytes from parent process", it)
        }
        openScope(ByteStreams.newDataInput(bytes), jagr) {
            SerializerFactory.getScoped<GradingRequest>(jagr).readScoped(this)
        }.toGradingQueue()
    }

    private fun GradingResult.sendToParent() {
        val outputStream = ByteArrayOutputStream(8192)
        val output = ByteStreams.newDataOutput(outputStream)
        Environment.stdOut.write(ProcessWorker.MARK_RESULT_BYTE)
        openScope(output, Jagr) {
            SerializerFactory.get<GradingResult>().write(this@sendToParent, this)
        }
        outputStream.writeTo(Environment.stdOut)
    }
}
