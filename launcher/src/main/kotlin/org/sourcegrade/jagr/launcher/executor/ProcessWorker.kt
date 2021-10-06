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
import org.sourcegrade.jagr.launcher.io.SerializerFactory
import org.sourcegrade.jagr.launcher.io.getScoped
import org.sourcegrade.jagr.launcher.io.openScope
import java.io.ByteArrayOutputStream
import java.io.File

class ProcessWorker(
  private val addActive: (Worker) -> Unit,
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
    .start()

  override fun assignJob(job: GradingJob) {
    val os = ByteArrayOutputStream()
    val output = ByteStreams.newDataOutput(os)
    openScope(output) {
      SerializerFactory.getScoped<GradingRequest>().writeScoped(job.request, this)
      openScope {

      }
    }
    os.writeTo(process.outputStream)
  }

  override fun kill() {
    process.destroy()
  }
}
