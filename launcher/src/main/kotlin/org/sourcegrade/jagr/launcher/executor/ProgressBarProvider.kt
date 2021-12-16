package org.sourcegrade.jagr.launcher.executor

abstract class ProgressBarProvider() {

    private val barChar = '='
    private val sideChar = '|'
    private val tipChar = '>'
    private val width = 120

    fun createProgressBar(progressDecimal: Double, barLengthFull: Int): String {
        val sb = this.adjustProgressBar(createBasicProgressBar(progressDecimal, barLengthFull))
        return sb.toString()
    }

    private fun createBasicProgressBar(progressDecimal: Double, barLengthFull: Int): StringBuilder {
        val barCount = barLengthFull * progressDecimal
        val sb = StringBuilder(30)
        sb.append(sideChar)
        val actualBarCount = barCount.toInt()
        for (i in 0 until actualBarCount) {
            sb.append(barChar)
        }
        if (progressDecimal < 1.0) {
            sb.append(tipChar)
        }
        return sb
    }

    protected abstract fun adjustProgressBar(sb: StringBuilder): StringBuilder
}
