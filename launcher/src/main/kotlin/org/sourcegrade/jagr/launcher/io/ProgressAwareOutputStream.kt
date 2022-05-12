/*
 *   Jagr - SourceGrade.org
 *   Copyright (C) 2021-2022 Alexander Staeding
 *   Copyright (C) 2021-2022 Contributors
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.sourcegrade.jagr.launcher.io

import kotlinx.coroutines.runBlocking
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
        if (enabled && b == newLine) {
            runBlocking {
                progressBar.print(delegate)
            }
        }
    }

    companion object {
        const val newLine = '\n'.code
        var progressBar: ProgressBar? = null
        var enabled = true
    }
}
