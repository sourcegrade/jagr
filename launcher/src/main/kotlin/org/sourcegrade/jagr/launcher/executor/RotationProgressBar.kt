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
