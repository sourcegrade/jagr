package org.jagrkt.common.testing

import org.jagrkt.api.testing.SourceFile
import org.jagrkt.api.testing.Submission
import org.jagrkt.api.testing.SubmissionInfo
import org.jagrkt.common.compiler.java.CompiledClass
import org.jagrkt.common.compiler.java.JavaSourceFile
import java.io.File

data class JavaSubmission(
  val file: File,
  private val info: SubmissionInfo,
  val compiledClasses: Map<String, CompiledClass>,
  val sourceFiles: Map<String, JavaSourceFile>,
) : Submission {
  override fun getInfo(): SubmissionInfo = info
  override fun getSourceFile(fileName: String): SourceFile? = sourceFiles[fileName]
  override fun toString(): String = "$info(${file.name})"
}
