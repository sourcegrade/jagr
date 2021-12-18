package org.sourcegrade.jagr.launcher.executor

fun interface ProgressBarProvider {

    fun transformProgressBar(sb: StringBuilder): StringBuilder

    companion object {
        const val BAR_CHAR = '='
        const val SIDE_CHAR = '|'
        const val TIP_CHAR = '>'
        const val WIDTH = 120
    }
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
