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

@file:Suppress("UnstableApiUsage")

package org.sourcegrade.jagr.launcher.executor

import com.google.common.io.ByteStreams
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.apache.logging.log4j.core.LogEvent
import org.slf4j.Logger
import org.sourcegrade.jagr.launcher.env.Environment
import org.sourcegrade.jagr.launcher.env.Jagr
import org.sourcegrade.jagr.launcher.env.logger
import org.sourcegrade.jagr.launcher.io.ProgressAwareOutputStream
import org.sourcegrade.jagr.launcher.io.SerializerFactory
import org.sourcegrade.jagr.launcher.io.get
import org.sourcegrade.jagr.launcher.io.getScoped
import org.sourcegrade.jagr.launcher.io.openScope
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.ObjectInputStream
import kotlin.reflect.KFunction2

class ProcessWorker(
    private val jagr: Jagr,
    private val removeActive: (Worker) -> Unit,
    processIODispatcher: CoroutineDispatcher,
) : Worker {
    override var job: GradingJob? = null
    override var status: WorkerStatus = WorkerStatus.PREPARING
    override var userTime: Long = 0

    private val jagrLocation: String = File(javaClass.protectionDomain.codeSource.location.toURI()).path

    private val process: Process = ProcessBuilder()
        .command("java", "-Dlog4j.configurationFile=log4j2-child.xml", "-jar", jagrLocation, "--child")
        .start()

    private val coroutineScope = CoroutineScope(processIODispatcher)

    init {
        status = WorkerStatus.READY
    }

    override fun assignJob(job: GradingJob) {
        check(this.job == null) { "Worker already has a job!" }
        status = WorkerStatus.RUNNING
        coroutineScope.launch {
            sendRequest(job.request)
            job.gradeCatching(jagr, ::receiveResult)
            status = WorkerStatus.FINISHED
            removeActive(this@ProcessWorker)
        }
        coroutineScope.launch {
            process.errorStream.reader().forEachLine {
                jagr.logger.error(it)
            }
        }
    }

    override fun kill() {
        process.destroy()
        coroutineScope.cancel("Killed by ProcessWorker")
    }

    private fun sendRequest(request: GradingRequest) {
        val outputStream = ByteArrayOutputStream(200_000)
        val output = ByteStreams.newDataOutput(outputStream)
        openScope(output, jagr) {
            SerializerFactory.getScoped<GradingRequest>(jagr).writeScoped(request, this)
        }
        outputStream.writeTo(process.outputStream)
        process.outputStream.close()
    }

    private fun receiveResult(request: GradingRequest): GradingResult? {
        val childProcessIn = process.inputStream
        while (true) {
            val next = childProcessIn.read()
            if (next == MARK_RESULT_BYTE) {
                break
            } else if (next == -1) {
                jagr.logger.error("${request.submission.info} :: Received unexpected EOF while waiting for child process to complete")
                return null
            } else if (next == MARK_LOG_MESSAGE_BYTE) {
                val ois = ObjectInputStream(childProcessIn)
                val event = ois.readObject() as LogEvent
                val throwable = ois.readObject() as Throwable? // the throwable field is not serialized in event or message
                ProgressAwareOutputStream.enabled = false
                jagr.logger.let<Logger, KFunction2<String, Throwable?, Unit>> {
                    when (event.level.intLevel() / 100) {
                        2 -> it::error
                        3 -> it::warn
                        4 -> it::info
                        5 -> it::debug
                        6 -> it::trace
                        else -> it::info
                    }
                }(event.message.formattedMessage, throwable)
                ProgressAwareOutputStream.enabled = true
                ProgressAwareOutputStream.progressBar?.print(Environment.stdOut)
            }
        }
        val bytes: ByteArray = runCatching { process.inputStream.readAllBytes() }.getOrElse {
            jagr.logger.error("${request.submission.info} :: Received IOException while waiting for child process to complete")
            return null
        }
        return openScope(ByteStreams.newDataInput(bytes), jagr) {
            SerializerFactory.getScoped<GradingRequest>(jagr).putInScope(request, this)
            SerializerFactory.get<GradingResult>(jagr).read(this)
        }
    }

    companion object {
        const val MARK_LOG_MESSAGE_BYTE = 2
        const val MARK_RESULT_BYTE = 7
    }
}
