package org.sourcegrade.jagr.launcher.executor

class RainbowProgressBar : RotationProgressBar() {
    // red, purple, blue, cyan, green, yellow
    override val rotationColors = arrayOf("\u001b[31m", "\u001B[35m", "\u001B[34m", "\u001B[36m", "\u001B[32m", "\u001B[33m")
}
