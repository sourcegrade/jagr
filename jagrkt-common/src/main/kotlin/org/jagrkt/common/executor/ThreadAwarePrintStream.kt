package org.jagrkt.common.executor

import java.io.OutputStream
import java.io.PrintStream

const val GRADING_THREAD_PREFIX = "G_Thread-"

class ThreadAwarePrintStream(delegate: OutputStream) : PrintStream(ThreadAwareOutputStream(delegate))

class ThreadAwareOutputStream(private val delegate: OutputStream) : OutputStream() {
  override fun write(b: Int) {
    if (!Thread.currentThread().name.startsWith(GRADING_THREAD_PREFIX)) {
      delegate.write(b)
    }
  }
}
