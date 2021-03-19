package org.jagrkt.common.context

import org.jagrkt.api.context.ContextResolver
import org.jagrkt.api.context.JavaClassContext
import org.jagrkt.api.context.JavaMethodContext
import org.jagrkt.api.context.PathContext
import org.jagrkt.api.context.SourceFileContext
import org.jagrkt.api.testing.Submission
import java.lang.reflect.Method
import java.util.concurrent.Callable

class ContextResolverFactory : ContextResolver.Factory {
  override fun ofPath(path: String?): ContextResolver<out PathContext> {
    TODO("Not yet implemented")
  }

  override fun ofSourceFile(filePath: String): ContextResolver<out SourceFileContext> {
    TODO("Not yet implemented")
  }

  override fun ofJavaClass(classSupplier: Callable<Class<*>>?): ContextResolver<out JavaClassContext> {
    TODO("Not yet implemented")
  }

  override fun ofJavaMethod(methodSupplier: Callable<Method>?): ContextResolver<out JavaMethodContext> {
    TODO("Not yet implemented")
  }

  private fun resolve(submission: Submission) {

  }
}
