package org.sourcegrade.jagr.launcher.io

import org.sourcegrade.jagr.launcher.executor.ProgressBar
import java.io.OutputStream
import java.io.PrintStream

class ProgressAwareOutputStream(private val delegate: PrintStream) : OutputStream() {

  override fun write(b: Int) {
    delegate.write(b)
    if (b == newLine) {
//      Environment.stdOut.print("hello".repeat(30) + '\r')
      progressBar?.print(delegate)
    }
  }

  companion object {
    const val newLine = '\n'.code
    var progressBar: ProgressBar? = null
  }
}
