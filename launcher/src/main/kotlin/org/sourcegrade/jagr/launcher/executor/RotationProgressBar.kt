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

package org.sourcegrade.jagr.launcher.executor

open class RotationProgressBar(private vararg val rotationColors: String) : ProgressBarProvider {

    init {
        require(rotationColors.isNotEmpty()) { "rotationColors may not be empty" }
    }

    private val reset = "\u001b[0m"

    private var startIndex = 0

    override fun transformProgressBar(sb: StringBuilder): StringBuilder {
        val tmp = StringBuilder(6 * sb.length)
        for ((i, elem) in sb.withIndex()) {
            tmp.append(rotationColors[(i + rotationColors.size - startIndex) % rotationColors.size])
            tmp.append(elem)
        }
        tmp.append(reset)
        startIndex = (startIndex + 1) % rotationColors.size
        return tmp
    }

    // red, purple, blue, cyan, green, yellow
    class Rainbow : RotationProgressBar("\u001b[31m", "\u001B[35m", "\u001B[34m", "\u001B[36m", "\u001B[32m", "\u001B[33m")

    // red, green
    class XMas : RotationProgressBar("\u001b[31m", "\u001B[32m")
}
