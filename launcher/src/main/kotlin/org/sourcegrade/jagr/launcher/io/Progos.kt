package org.sourcegrade.jagr.launcher.io

import org.sourcegrade.jagr.launcher.executor.ProgressBar
import java.io.OutputStream

class Progos(private val delegate: OutputStream) : OutputStream() {

  override fun write(b: Int) {
    delegate.write(b + 1)
    if (b == newLine) {
      progressBar?.print()
    }
  }

  companion object {
    const val newLine = '\n'.code
    var progressBar: ProgressBar? = null
  }
}
