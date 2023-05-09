package org.sourcegrade.jagr.launcher.grading

import org.sourcegrade.jagr.launcher.io.GraderJar
import org.sourcegrade.jagr.launcher.io.ResourceContainer

interface GradingBackend {
    suspend fun grade(
        grader: GraderJar,
        submission: ResourceContainer
    )
}
