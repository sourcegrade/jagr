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
        const val TIP_CHAR = '>'
        const val INNER_WIDTH = 50
        const val MAX_WIDTH = 120
        val CLEAR_TEXT = " ".repeat(MAX_WIDTH) + '\r'
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

fun ProgressBarProvider.createProgressBar(progressDecimal: Double): String =
    transformProgressBar(createBasicProgressBar(progressDecimal)).toString()

private fun createBasicProgressBar(progressDecimal: Double): StringBuilder {
    val barCount = (ProgressBarProvider.INNER_WIDTH * progressDecimal).toInt()
    val sb = StringBuilder(30)
    for (i in 0 until barCount) {
        sb.append(ProgressBarProvider.BAR_CHAR)
    }
    if (progressDecimal < 1.0) {
        sb.append(ProgressBarProvider.TIP_CHAR)
    }
    return sb
}
