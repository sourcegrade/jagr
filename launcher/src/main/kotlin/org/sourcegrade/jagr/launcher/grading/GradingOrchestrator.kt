package org.sourcegrade.jagr.launcher.grading

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.sourcegrade.jagr.api.rubric.GradedRubric
import org.sourcegrade.jagr.launcher.compiler.phisn.GraderCompiler
import org.sourcegrade.jagr.launcher.io.GraderJar
import org.sourcegrade.jagr.launcher.io.ResourceContainer

class GradingOrchestrator(
    private val backend: GradingBackend,
    private val graderCompiler: GraderCompiler,
) {
    suspend fun schedule(
        library: ResourceContainer,
        grader: ResourceContainer,
        submissions: List<ResourceContainer>,
    ): GradedRubric =
        schedule(
            grader = graderCompiler.compile(library, grader),
            submissions = submissions,
        )

    suspend fun schedule(
        grader: GraderJar,
        submissions: List<ResourceContainer>,
    ): List<GradedRubric> = coroutineScope {
        return@coroutineScope submissions
            .map { async { backend.grade(grader, it) } }
            .awaitAll()
    }
}
