package org.jagrkt.common.context

import org.jagrkt.api.context.JavaClassContext
import org.jagrkt.api.context.JavaContext
import org.jagrkt.api.context.JavaMethodContext
import org.jagrkt.api.context.SourceFileContext
import org.jagrkt.api.testing.SourceFile
import org.jagrkt.api.testing.TestCycle
import java.lang.reflect.Method

class JavaMethodContextImpl(
  testCycle: TestCycle,
  parent: JavaClassContext,
  modifiedSourceFile: SourceFile?,
  originalSourceFile: SourceFile?,
  modifiedSource: String?,
  originalSource: String?,
  lineNumber: Int,
  private val solutionClassLoader: ClassLoader,
  private val classLoader: ClassLoader,
  private val modifiedMethod: Method?,
  private val originalMethod: Method?,
) : SourceFileContextImpl<JavaClassContext>(
  testCycle,
  parent,
  parent.path, // a method has the same path as its declaring class
  modifiedSourceFile,
  originalSourceFile,
  modifiedSource,
  originalSource,
  lineNumber,
), JavaMethodContext, JavaContext {
  override fun getSolutionClassLoader(): ClassLoader = solutionClassLoader
  override fun getClassLoader(): ClassLoader = classLoader
  override fun getFileContext(): SourceFileContext = parent.fileContext
  override fun getOriginalMethod(): Method = checkNotNull(originalMethod) { "does not exist in solution" }
  override fun getModifiedMethod(): Method = checkNotNull(modifiedMethod) { "does not exist in submission" }
  override fun getName(): String = getModifiedMethod().name
}
