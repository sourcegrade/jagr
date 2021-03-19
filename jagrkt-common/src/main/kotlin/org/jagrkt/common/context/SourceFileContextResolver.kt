package org.jagrkt.common.context

import org.jagrkt.api.context.ContextResolver
import org.jagrkt.api.context.SourceFileContext
import org.jagrkt.api.testing.TestCycle
import java.nio.file.Path

class SourceFileContextResolver(
  private val path: Path,
) : ContextResolver<SourceFileContext> {
  override fun resolve(testCycle: TestCycle): SourceFileContext {
    TODO("Not yet implemented")
  }
}
