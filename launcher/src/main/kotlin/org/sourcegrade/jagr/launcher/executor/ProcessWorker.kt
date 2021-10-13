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
import org.sourcegrade.jagr.api.testing.Submission
import org.sourcegrade.jagr.launcher.env.Jagr
import org.sourcegrade.jagr.launcher.env.logger
import org.sourcegrade.jagr.launcher.io.SerializerFactory
import org.sourcegrade.jagr.launcher.io.get
import org.sourcegrade.jagr.launcher.io.getScoped
import org.sourcegrade.jagr.launcher.io.keyOf
import org.sourcegrade.jagr.launcher.io.openScope
import java.io.ByteArrayOutputStream
import java.io.EOFException
import java.io.File

class ProcessWorker(
  private val jagr: Jagr,
  processIODispatcher: CoroutineDispatcher,
  private val removeActive: (Worker) -> Unit,
) : Worker {
  override var job: GradingJob? = null
  override var status: WorkerStatus = WorkerStatus.PREPARING
  override var userTime: Long = 0

  private val jagrLocation: String = File(javaClass.protectionDomain.codeSource.location.toURI()).path

  init {
    println("Starting process 'java -jar $jagrLocation'")
  }

  private val process: Process = ProcessBuilder()
    .command("java", "-jar", jagrLocation, "--child")
//    .redirectError(File("error.txt"))
//    .redirectOutput(File("output.txt"))
    .start()

  private val coroutineScope = CoroutineScope(processIODispatcher)

  override fun assignJob(job: GradingJob) {
    coroutineScope.launch {
      sendRequest(job.request)
      Jagr.logger.info("Sent request, waiting...")
      val result = receiveResult(job)
      Jagr.logger.info("Received result: $result")
      job.result.complete(result)
      removeActive(this@ProcessWorker)
    }
    coroutineScope.launch {
      process.errorStream.reader().forEachLine {
        Jagr.logger.error(it)
      }
    }
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
    // ignore bytes until 7
    val stdIn = process.inputStream
    Jagr.logger.info("Throwing bytes until 7")
    while (true) {
      val next = stdIn.read()
      if (next == 7) {
        Jagr.logger.info("Received byte 7")
        break
      } else
        if (next == -1) {
          Jagr.logger.error("Received unexpected EOF while waiting for child process to complete")
          job.result.completeExceptionally(EOFException())
        }
    }
    return openScope(ByteStreams.newDataInput(process.inputStream.readAllBytes()), jagr) {
      SerializerFactory.getScoped<GradingRequest>(jagr).putInScope(job.request, this)
      SerializerFactory.get<GradingResult>(jagr).read(this)
    }
  }

  override fun kill() {
    process.destroy()
    coroutineScope.cancel("Killed by ProcessWorker")
  }
}
