package org.sourcegrade.jagr.launcher.executor

abstract class RotationProgressBar : ProgressBarProvider {
    private val reset = "\u001b[0m"

    abstract val rotationColors: Array<String>
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
}
