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

fun interface ProgressBarProvider {

    fun transformProgressBar(sb: StringBuilder): StringBuilder

    companion object {
        const val BAR_CHAR = '='
        const val SIDE_CHAR = '|'
        const val TIP_CHAR = '>'
        const val WIDTH = 120
    }

    object Default : ProgressBarProvider {
        override fun transformProgressBar(sb: StringBuilder): StringBuilder = sb
    }
}

fun createProgressBarProvider(name: String?): ProgressBarProvider = when (name) {
    "rainbow" -> RotationProgressBar.Rainbow()
    "xmas" -> RotationProgressBar.XMas()
    null -> ProgressBarProvider.Default
    else -> error("Could not find progress bar provider $name")
}

fun ProgressBarProvider.createProgressBar(progressDecimal: Double, barLengthFull: Int): String =
    transformProgressBar(createBasicProgressBar(progressDecimal, barLengthFull)).toString()

private fun createBasicProgressBar(progressDecimal: Double, barLengthFull: Int): StringBuilder {
    val barCount = barLengthFull * progressDecimal
    val sb = StringBuilder(30)
    sb.append(ProgressBarProvider.SIDE_CHAR)
    val actualBarCount = barCount.toInt()
    for (i in 0 until actualBarCount) {
        sb.append(ProgressBarProvider.BAR_CHAR)
    }
    if (progressDecimal < 1.0) {
        sb.append(ProgressBarProvider.TIP_CHAR)
    }
    return sb
}
