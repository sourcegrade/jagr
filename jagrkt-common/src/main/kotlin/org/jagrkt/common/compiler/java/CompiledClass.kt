package org.jagrkt.common.compiler.java

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.net.URI
import javax.tools.JavaFileObject
import javax.tools.SimpleJavaFileObject

sealed class CompiledClass(val className: String) : SimpleJavaFileObject(URI(className), JavaFileObject.Kind.CLASS) {

  abstract val byteArray: ByteArray
  var source: JavaSourceFile? = null

  class Runtime(className: String) : CompiledClass(className) {
    private val outputStream = ByteArrayOutputStream()
    override val byteArray: ByteArray get() = outputStream.toByteArray()
    override fun openOutputStream(): OutputStream = outputStream
  }

  class Existing(className: String, override val byteArray: ByteArray) : CompiledClass(className) {
    override fun openInputStream(): InputStream = ByteArrayInputStream(byteArray)
  }
}
