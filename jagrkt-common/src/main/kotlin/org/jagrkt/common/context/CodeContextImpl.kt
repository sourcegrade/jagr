package org.jagrkt.common.context

import org.jagrkt.api.context.CodeContext
import org.jagrkt.api.testing.Submission
import org.jagrkt.api.testing.TestCycle
import java.nio.file.Path

open class CodeContextImpl<P : CodeContext>(
  private val testCycle: TestCycle,
  private val parent: P,
  private val path: Path,
  private val exists: Boolean,
  private val matched: Boolean,
  private val modified: Boolean,
) : CodeContext {
  override fun getTestCycle(): TestCycle = testCycle
  override fun getPath(): Path = path
  override fun exists(): Boolean = exists
  override fun matched(): Boolean = matched
  override fun modified(): Boolean = exists && matched && modified
  override fun getParent(): P = parent
}
