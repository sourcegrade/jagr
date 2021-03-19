package org.jagrkt.common.context

import org.jagrkt.api.context.ContextResolver
import org.jagrkt.api.context.JavaMethodContext
import org.jagrkt.api.testing.Submission
import org.jagrkt.api.testing.TestCycle
import org.jagrkt.common.testing.JavaSubmission
import org.jagrkt.common.testing.JavaTestCycle
import java.lang.reflect.Method
import kotlin.streams.asSequence

class JavaMethodContextResolver(
  private val methodSupplier: () -> Method,
) : ContextResolver<JavaMethodContext> {

  override fun resolve(testCycle: TestCycle): JavaMethodContext {
    require(testCycle is JavaTestCycle) { "JavaMethodContext only applicable to java submissions" }
    val originalMethod = methodSupplier()
    val originalDeclaringClass = originalMethod.declaringClass
    val declaringClassName = originalDeclaringClass.name
    val modifiedDeclaringClass = testCycle.classLoader.loadClass(declaringClassName)
    val modifiedMethod = modifiedDeclaringClass.getMethod(originalMethod.name, *originalMethod.parameterTypes)
    val declaringClassContext = JavaClassContextResolver { originalDeclaringClass }.resolve(testCycle)

    val modifiedSourceFile = testCycle.submission.compiledClasses[declaringClassName]?.source

    val modifiedSource = modifiedSourceFile?.content?.extractMethodSource(modifiedMethod)

    return JavaMethodContextImpl(
      testCycle,
      declaringClassContext,
      modifiedSourceFile,
      null, // TODO
      modifiedSource?.first,
      null,
      modifiedSource?.second ?: -1,
      testCycle.classLoader, // TODO
      testCycle.classLoader,
      modifiedMethod,
      originalMethod,
    )
  }
}

val parameterNameRegex = Regex("([a-z][A-Z])([a-z][A-Z][0-9][_-])*")

fun String.extractMethodSource(method: Method): Pair<String, Int> {
  val matchResult = requireNotNull(method.signaturePattern().find(this)) { "method not found" }
  val start = matchResult.range.first
  val end = matchResult.range.last

  var lineNumber = 0
  chars().asSequence().forEach {
    if (it == '\n'.toInt()) {
      ++lineNumber
    }
  }
  return "a" to 0
}

fun Method.signaturePattern(): Regex {
  val allParameters = parameters.joinToString(", ") { "${it.type.simpleName} ${parameterNameRegex.pattern}" }
  return Regex("${returnType.simpleName} $name\\($allParameters\\)")
}
