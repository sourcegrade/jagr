package org.sourcegrade.jagr.launcher.io

import org.sourcegrade.jagr.launcher.executor.ProgressBar
import java.io.OutputStream
import java.io.PrintStream
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class ProgressAwareOutputStream(private val delegate: PrintStream) : OutputStream() {

    private val lock = ReentrantLock()

    override fun write(b: Int) = lock.withLock {
        progressBar?.let { writeWithProgress(it, b) } ?: delegate.write(b)
    }

    private fun writeWithProgress(progressBar: ProgressBar, b: Int) {
        delegate.write(b)
        if (b == newLine) {
            progressBar.print(delegate)
        }
    }

    companion object {
        const val newLine = '\n'.code
        var progressBar: ProgressBar? = null
    }
}
