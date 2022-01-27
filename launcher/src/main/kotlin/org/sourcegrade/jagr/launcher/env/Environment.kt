/*
 *   Jagr - SourceGrade.org
 *   Copyright (C) 2021 Alexander Staeding
 *   Copyright (C) 2021 Contributors
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

package org.sourcegrade.jagr.launcher.env

import org.jetbrains.annotations.ApiStatus
import org.sourcegrade.jagr.launcher.io.ProgressAwareOutputStream
import java.io.OutputStream
import java.io.PrintStream

@ApiStatus.Internal
object Environment {
    private val actualStdOut = System.out
    var stdOut: PrintStream = actualStdOut
        private set

    fun initializeChildProcess() {
        Jagr.logger
        setWasteBasket()
    }

    fun initializeMainProcess() {
        stdOut = PrintStream(ProgressAwareOutputStream(stdOut))
        System.setOut(stdOut)
        Jagr.logger
        setWasteBasket()
    }

    private fun setWasteBasket() {
        val wasteBasket = PrintStream(OutputStream.nullOutputStream())
        System.setOut(wasteBasket)
        // System.setErr(wasteBasket)
    }

    fun cleanupMainProcess() {
        stdOut = actualStdOut
    }
}
