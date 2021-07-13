/*
 *   JagrKt - JagrKt.org
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

package org.jagrkt.common.compiler.java

import org.jagrkt.api.inspect.Element
import org.jagrkt.api.testing.CompileResult
import org.jagrkt.common.testing.SubmissionInfoImpl
import org.slf4j.Logger
import java.io.File

data class JavaCompileResult(
  val file: File,
  val submissionInfo: SubmissionInfoImpl? = null,
  val sourceFiles: Map<String, JavaSourceFile> = mapOf(),
  val compiledClasses: Map<String, CompiledClass> = mapOf(),
  val libClasses: Map<String, CompiledClass> = mapOf(),
  val elementTable: Array<Element> = emptyArray(),
  private val messages: List<String> = listOf(),
  private val warningCount: Int = 0,
  private val errorCount: Int = 0,
  private val otherCount: Int = 0,
) : CompileResult {
  override fun getMessages(): List<String> = messages
  override fun getWarningCount(): Int = warningCount
  override fun getErrorCount(): Int = errorCount
  override fun getOtherCount(): Int = otherCount

  fun printMessages(logger: Logger, lazyError: () -> String, lazyWarning: () -> String) {
    when {
      errorCount > 0 -> {
        logger.error(lazyError())
        messages.forEach(logger::error)
      }
      warningCount > 0 -> {
        logger.warn(lazyWarning())
        messages.forEach(logger::warn)
      }
    }
  }
}
