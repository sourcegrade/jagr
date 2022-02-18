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

package org.sourcegrade.jagr.launcher.executor

import kotlinx.coroutines.runBlocking
import java.io.PrintStream
import java.text.DecimalFormat
import java.time.Duration
import java.time.Instant

class ProgressBar(
    private val rubricCollector: RubricCollector,
    private val progressBarProvider: ProgressBarProvider,
    private val showElementsIfLessThan: Int = 3,
) {

    private val decimalFormat = DecimalFormat("00.00")
    private val gradingStart = Instant.now()
    private val bufferSize = 32
    private val deltaBuffer = IntArray(bufferSize)
    private val timeBuffer = arrayOfNulls<Instant>(bufferSize)
    private var bufferPos = 0
    private var lastFinished = 0

    /**
     * The amount of time a single submission takes to grade, averaged over the last [bufferSize] - 1 submissions
     */
    private fun calculateVelocity(): Duration {
        val all = mutableListOf<Duration>()
        for (i in 1 until bufferSize) {
            val posA = (bufferPos + i - 1 + bufferSize) % bufferSize
            val posB = (bufferPos + i + bufferSize) % bufferSize
            val start = timeBuffer[posA] ?: continue
            val end = timeBuffer[posB] ?: continue
            deltaBuffer[posA].let {
                if (it != 0) {
                    all += Duration.between(start, end).multipliedBy(it.toLong())
                }
            }
        }
        return if (all.isEmpty()) {
            Duration.ZERO
        } else {
            all.fold(Duration.ZERO, Duration::plus).dividedBy(all.size.toLong())
        }
    }

    suspend fun print(out: PrintStream) = rubricCollector.withSnapshot { snapshot ->
        val finished = snapshot.gradingFinished.size
        val total = snapshot.total
        val progressDecimal = finished.toDouble() / total.toDouble().coerceAtLeast(0.0)
        val formattedPercentage = decimalFormat.format(progressDecimal * 100.0)
        val barCount = (ProgressBarProvider.INNER_WIDTH * progressDecimal).toInt()
        val sb = StringBuilder(30)
        // gray
        sb.append("\u001b[38;5;250m")
        sb.append('[')
        sb.append(progressBarProvider.createProgressBar(progressDecimal))
        for (i in barCount until ProgressBarProvider.INNER_WIDTH) {
            sb.append(' ')
        }
        sb.append("\u001b[38;5;250m")
        sb.append(']')
        sb.append(' ')
        sb.append(formattedPercentage)
        sb.append('%')
        sb.append(" ($finished/$total)")
        if (snapshot.gradingScheduled.size in 1 until showElementsIfLessThan) {
            sb.append(" Remaining: [${snapshot.gradingScheduled.joinToString { it.request.submission.toString() }}]")
        }
        // if more submissions are finished, save the time and how many submissions were graded since last save
        if (finished != lastFinished) {
            deltaBuffer[bufferPos] = finished - lastFinished
            timeBuffer[bufferPos] = Instant.now()
            bufferPos = (bufferPos + 1) % bufferSize
            lastFinished = finished
        }
        val now = Instant.now()
        val elapsed = Duration.between(gradingStart, now)
        val estimatedTotal = calculateVelocity().multipliedBy(total.toLong())
        val estimatedLeft = estimatedTotal.minus(elapsed)
        sb.append(" ", elapsed.formatted, " / ", estimatedTotal.formatted, " (", estimatedLeft.formatted, " remaining)")
        // pad with spaces
        sb.append(" ".repeat((ProgressBarProvider.MAX_WIDTH - sb.length).coerceAtLeast(0)))
        out.print(sb.toString() + '\r')
    }

    fun clear(out: PrintStream) = out.print(ProgressBarProvider.CLEAR_TEXT)
}

val Duration.formatted: String
    get() = "${toHours()}".padStart(2, '0') + ":" +
        "${toMinutesPart()}".padStart(2, '0') + ":" +
        "${toSecondsPart()}".padStart(2, '0')
