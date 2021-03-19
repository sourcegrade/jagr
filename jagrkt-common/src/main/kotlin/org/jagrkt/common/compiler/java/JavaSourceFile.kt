package org.jagrkt.common.compiler.java

import org.jagrkt.api.testing.SourceFile
import java.io.InputStream
import java.net.URI
import javax.tools.JavaFileObject.Kind
import javax.tools.SimpleJavaFileObject

class JavaSourceFile(
  private val className: String,
  private val fileName: String,
  inputStream: InputStream,
) : SimpleJavaFileObject(URI.create("string:///$fileName"), Kind.SOURCE), SourceFile {
  private val content: String = inputStream.bufferedReader().readText()
  override fun getFileName(): String = fileName
  override fun getContent(): String = content
  override fun getClassName(): String = className
  override fun getCharContent(ignoreEncodingErrors: Boolean): CharSequence = content
}
