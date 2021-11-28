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
import org.slf4j.Logger
import org.sourcegrade.jagr.launcher.env.Jagr
import org.sourcegrade.jagr.launcher.env.logger
import org.sourcegrade.jagr.launcher.io.SerializerFactory
import org.sourcegrade.jagr.launcher.io.get
import org.sourcegrade.jagr.launcher.io.getScoped
import org.sourcegrade.jagr.launcher.io.openScope
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset
import kotlin.reflect.KFunction
import kotlin.reflect.KFunction1

class ProcessWorker(
  private val jagr: Jagr,
  private val runtimeGrader: RuntimeGrader,
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
      try {
        job.result.complete(receiveResult(job))
      } catch (e: Exception) {
        job.result.completeExceptionally(e)
      }
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

  private fun receiveResult(job: GradingJob): GradingResult {
    val startedUtc = OffsetDateTime.now(ZoneOffset.UTC).toInstant()
    val childProcessIn = process.inputStream
    while (true) {
      val next = childProcessIn.read()
      if (next == MARK_RESULT_BYTE) {
        break
      } else if (next == -1) {
        jagr.logger.error("${job.request.submission.info} :: Received unexpected EOF while waiting for child process to complete")
        return createFallbackResult(startedUtc, job.request)
      } else if (next == MARK_LOG_MESSAGE_BYTE) {
        val level = childProcessIn.read()
        val length = childProcessIn.read() shl 24 or childProcessIn.read() shl 16 or childProcessIn.read() shl 8 or childProcessIn.read()
        if (length < 0) {
          jagr.logger.error("${job.request.submission.info} :: Received IOException while waiting for child process to complete")
          return createFallbackResult(startedUtc, job.request)
        }
        val message: String = runCatching { process.inputStream.readNBytes(length) }.getOrElse {
          jagr.logger.error("${job.request.submission.info} :: Received IOException while waiting for child process to complete")
          return createFallbackResult(startedUtc, job.request)
        }.toString(StandardCharsets.UTF_8)
        jagr.logger.let<Logger, KFunction1<String, Unit>> {
          when (level) {
            2 -> it::error
            3 -> it::warn
            4 -> it::info
            5 -> it::debug
            6 -> it::trace
            else -> it::info
          }
        }(message)
      }
    }
    val bytes: ByteArray = runCatching { process.inputStream.readAllBytes() }.getOrElse {
      jagr.logger.error("${job.request.submission.info} :: Received IOException while waiting for child process to complete")
      return createFallbackResult(startedUtc, job.request)
    }
    return openScope(ByteStreams.newDataInput(bytes), jagr) {
      SerializerFactory.getScoped<GradingRequest>(jagr).putInScope(job.request, this)
      SerializerFactory.get<GradingResult>(jagr).read(this)
    }
  }

  private fun createFallbackResult(startedUtc: Instant, request: GradingRequest): GradingResult {
    val finishedUtc = OffsetDateTime.now(ZoneOffset.UTC).toInstant()
    val fallbackRubrics = runtimeGrader.gradeFallback(request.graders, request.submission)
    return GradingResult(startedUtc, finishedUtc, request, fallbackRubrics)
  }

  companion object {
    const val MARK_LOG_MESSAGE_BYTE = 2
    const val MARK_RESULT_BYTE = 7
  }
}
