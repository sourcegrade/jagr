package org.jagrkt.common.compiler.java

import javax.tools.FileObject
import javax.tools.ForwardingJavaFileManager
import javax.tools.JavaFileManager
import javax.tools.JavaFileObject

class ExtendedStandardJavaFileManager(
  javaFileManager: JavaFileManager,
  private val classPath: Map<String, CompiledClass>,
  private val classStorage: MutableMap<String, CompiledClass.Runtime>,
) : ForwardingJavaFileManager<JavaFileManager>(javaFileManager) {

  override fun getJavaFileForOutput(
    location: JavaFileManager.Location,
    className: String,
    kind: JavaFileObject.Kind,
    sibling: FileObject,
  ): JavaFileObject {
    try {
      val compiledClass = CompiledClass.Runtime(className)
      classStorage[compiledClass.name] = compiledClass
      return compiledClass
    } catch (e: Exception) {
      throw IllegalStateException("Error creating class file for $className", e)
    }
  }

  override fun list(
    location: JavaFileManager.Location?,
    packageName: String?,
    kinds: MutableSet<JavaFileObject.Kind>,
    recurse: Boolean,
  ): Iterable<JavaFileObject> {
    val fromDelegate = super.list(location, packageName, kinds, recurse)
    if (packageName != null
      && kinds.contains(JavaFileObject.Kind.CLASS)
      && location != null
      && location.name == "CLASS_PATH"
    ) {
      return fromDelegate + getCompiledClassesFromClassPath(packageName)
    }
    return fromDelegate
  }

  private fun getCompiledClassesFromClassPath(packageName: String): List<JavaFileObject> {
    return classPath.asSequence()
      .filter { it.key.startsWith(packageName) }
      .map { it.value }
      .toList()
  }

  override fun inferBinaryName(location: JavaFileManager.Location, file: JavaFileObject): String {
    if (file is CompiledClass.Existing) {
      return file.className
    }
    return super.inferBinaryName(location, file)
  }
}
