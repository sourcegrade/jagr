package org.jagrkt.common.context

import org.jagrkt.api.context.CodeContext
import org.jagrkt.api.context.SourceFileContext
import org.jagrkt.api.testing.SourceFile
import org.jagrkt.api.testing.Submission
import org.jagrkt.api.testing.TestCycle
import java.nio.file.Path

open class SourceFileContextImpl<P : CodeContext>(
  testCycle: TestCycle,
  parent: P,
  path: Path,
  private val modifiedSourceFile: SourceFile?,
  private val originalSourceFile: SourceFile?,
  private val modifiedSource: String?,
  private val originalSource: String?,
  private val lineNumber: Int = 0,
) : CodeContextImpl<P>(
  testCycle,
  parent,
  path,
  exists = modifiedSource != null,
  matched = originalSource != null,
  modified = modifiedSource != originalSource
), SourceFileContext {
  override fun getModifiedSourceFile(): SourceFile = checkNotNull(modifiedSourceFile) { "modified source file" }
  override fun getOriginalSourceFile(): SourceFile = checkNotNull(originalSourceFile) { "original source file" }
  override fun getModifiedSource(): String = checkNotNull(modifiedSource) { "modified source" }
  override fun getOriginalSource(): String = checkNotNull(originalSource) { "original source" }
  override fun getLineNumber(): Int = lineNumber
}
