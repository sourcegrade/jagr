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

package org.sourcegrade.jagr.core.executor

import com.google.inject.Inject
import org.slf4j.Logger
import org.sourcegrade.jagr.api.testing.Submission
import org.sourcegrade.jagr.core.compiler.ResourceExtractor
import org.sourcegrade.jagr.core.compiler.java.JavaCompiledContainer
import org.sourcegrade.jagr.core.compiler.java.JavaSourceFile
import org.sourcegrade.jagr.core.compiler.java.RuntimeJarLoader
import org.sourcegrade.jagr.core.compiler.java.RuntimeResources
import org.sourcegrade.jagr.core.compiler.java.loadCompiled
import org.sourcegrade.jagr.core.compiler.submissionInfo
import org.sourcegrade.jagr.core.parallelMapNotNull
import org.sourcegrade.jagr.core.testing.GraderInfoImpl
import org.sourcegrade.jagr.core.testing.GraderJarImpl
import org.sourcegrade.jagr.core.testing.JavaSubmission
import org.sourcegrade.jagr.core.testing.SubmissionInfoImpl
import org.sourcegrade.jagr.core.transformer.CommonClassTransformer
import org.sourcegrade.jagr.core.transformer.SubmissionVerificationTransformer
import org.sourcegrade.jagr.core.transformer.TransformationApplier
import org.sourcegrade.jagr.core.transformer.applierOf
import org.sourcegrade.jagr.core.transformer.plus
import org.sourcegrade.jagr.core.transformer.useWhen
import org.sourcegrade.jagr.launcher.io.GraderJar
import org.sourcegrade.jagr.launcher.io.GradingBatch
import org.sourcegrade.jagr.launcher.io.ResourceContainer

data class CompiledBatch(
    val batch: GradingBatch,
    val graders: List<GraderJar>,
    val submissions: List<Submission>,
    val libraries: RuntimeResources,
)

class CompiledBatchFactoryImpl @Inject constructor(
    private val logger: Logger,
    private val runtimeJarLoader: RuntimeJarLoader,
    private val commonClassTransformer: CommonClassTransformer,
) {

    fun compile(batch: GradingBatch): CompiledBatch {
        val libraries = runtimeJarLoader.loadCompiled(batch.libraries)
        val commonTransformerApplier = applierOf(commonClassTransformer)
        val graders: List<GraderJarImpl> = batch.graders.compile(
            commonTransformerApplier,
            libraries,
            "grader",
            GraderInfoImpl.Extractor,
        ) {
            if (errors == 0) GraderJarImpl(logger, this, libraries) else null
        }
        val submissionTransformerApplier = createTransformerApplierFromGraders(graders)
        val submissionFileOverrides = calculateSubmissionFileOverrides(graders)

        // maps assignment ids to files that should be replaced with solution files
        val replacements = submissionFileOverrides.mapValues { (assignmentId, solutionOverrides) ->
            val gradersForAssignment = graders.filter { it.info.assignmentIds.contains(assignmentId) }
            solutionOverrides.mapNotNull { solutionOverride ->
                gradersForAssignment.firstNotNullOfOrNull { it.container.source.sourceFiles[solutionOverride] }
            }.associateBy { it.fileName }
        }

        val submissions: List<Submission> = batch.submissions.compile(
            submissionTransformerApplier,
            libraries,
            "submission",
            SubmissionInfoImpl.Extractor,
            replacements,
        ) submissionCompile@{
            val submissionInfo = this.submissionInfo
            if (submissionInfo == null) {
                logger.error("${info.name} does not have a submission-info.json! Skipping...")
                return@submissionCompile null
            }
            JavaSubmission(submissionInfo, this, libraries)
        }
        return CompiledBatch(batch, graders, submissions, libraries)
    }

    /**
     * Create a transformer applier that selectively applies transformations to
     * submissions only if the grader contains a rubric for it
     */
    private fun createTransformerApplierFromGraders(graders: List<GraderJar>): TransformationApplier {
        val base = applierOf(SubmissionVerificationTransformer(), commonClassTransformer)
        return graders.map { graderJar ->
            graderJar.configuration.transformers useWhen { result ->
                result.submissionInfo?.assignmentId?.let(graderJar.info.assignmentIds::contains) == true
            }
        }.fold(base) { a, b -> a + b }
    }

    private fun calculateSubmissionFileOverrides(graders: List<GraderJar>): Map<String, List<String>> {
        return graders.map { graderJar ->
            graderJar.info.assignmentIds.flatMap { assignmentId ->
                graderJar.configuration.fileNameSolutionOverrides.map { solutionOverride ->
                    assignmentId to solutionOverride
                }
            }.groupBy({ it.first }, { it.second })
        }.fold(emptyMap()) { acc, map -> acc + map }
    }

    private fun <T> Sequence<ResourceContainer>.compile(
        transformerApplier: TransformationApplier,
        libraries: RuntimeResources,
        containerType: String,
        resourceExtractor: ResourceExtractor = ResourceExtractor { _, _, _, _ -> },
        replacements: Map<String, Map<String, JavaSourceFile>> = mapOf(),
        constructor: JavaCompiledContainer.() -> T?,
    ): List<T> = toList().parallelMapNotNull {
        val originalSources = runtimeJarLoader.loadSources(it, resourceExtractor)
        val submissionInfo = originalSources.submissionInfo
        val replacementsForAssignment = if (replacements.isNotEmpty() && submissionInfo != null) {
            replacements[submissionInfo.assignmentId]
        } else null
        val replacedSources = if (replacementsForAssignment == null) {
            originalSources
        } else {
            originalSources.copy(
                sourceFiles = originalSources.sourceFiles + replacementsForAssignment
            )
        }
        val original = runtimeJarLoader.compileSources(replacedSources, libraries)
        val transformed = try {
            transformerApplier.transform(original)
        } catch (e: Throwable) {
            // create a copy of the original compile result but throw out runtime resources (compiled classes and resources)
            original.copy(
                runtimeResources = RuntimeResources(),
                messages = listOf("Transformation failed :: ${e.message}") + original.messages,
                errors = original.errors + 1,
            )
        }
        with(transformed) {
            printMessages(
                logger,
                { "$containerType ${info.name} has $warnings warnings and $errors errors!" },
            ) { "$containerType ${info.name} has $warnings warnings!" }
            try {
                constructor()?.apply { logger.info("Loaded $containerType ${it.info.name}") }
                    ?: run { logger.error("Failed to load $containerType ${it.info.name}"); null }
            } catch (e: Exception) {
                logger.error("An error occurred loading $containerType ${it.info.name}", e)
                null
            }
        }
    }
}
