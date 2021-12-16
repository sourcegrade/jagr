package org.sourcegrade.jagr.launcher.executor

class XMasProgressBar() : RotationProgressBar() {
    // red, green,
    override val rotationColors = arrayOf("\u001b[31m", "\u001B[32m")
}
