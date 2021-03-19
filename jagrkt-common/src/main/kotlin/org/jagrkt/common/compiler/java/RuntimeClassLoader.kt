package org.jagrkt.common.compiler.java

class RuntimeClassLoader(
  private val classStorage: Map<String, CompiledClass>,
  parent: ClassLoader = getSystemClassLoader(),
) : ClassLoader(parent) {

  @Throws(ClassNotFoundException::class, ClassFormatError::class)
  override fun findClass(name: String): Class<*> {
    val compiledClass = classStorage[name] ?: return super.findClass(name)
    val byteCode: ByteArray = compiledClass.byteArray
    return defineClass(name, byteCode, 0, byteCode.size)
  }
}
