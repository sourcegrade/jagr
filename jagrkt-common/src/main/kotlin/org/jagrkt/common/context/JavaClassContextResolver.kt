package org.jagrkt.common.context

import org.jagrkt.api.context.ContextResolver
import org.jagrkt.api.context.JavaClassContext
import org.jagrkt.api.testing.TestCycle

class JavaClassContextResolver(
  classSupplier: () -> Class<*>,
) : ContextResolver<JavaClassContext> {
  override fun resolve(testCycle: TestCycle): JavaClassContext {
    TODO("Not yet implemented")
  }
}
