package org.sourcegrade.jagr

import com.google.inject.Inject
import org.apache.logging.log4j.Logger
import org.sourcegrade.jagr.api.testing.ClassTransformerOrder
import org.sourcegrade.jagr.api.testing.Submission
import org.sourcegrade.jagr.core.compiler.InfoJsonResourceExtractor
import org.sourcegrade.jagr.core.compiler.ResourceExtractor
import org.sourcegrade.jagr.core.compiler.RuntimeContainer
import org.sourcegrade.jagr.core.compiler.java.*
import org.sourcegrade.jagr.core.compiler.submissionInfo
import org.sourcegrade.jagr.core.testing.GraderJarImpl
import org.sourcegrade.jagr.core.testing.JavaSubmission
import org.sourcegrade.jagr.core.transformer.*
import org.sourcegrade.jagr.launcher.io.GraderJar
import org.sourcegrade.jagr.launcher.io.ResourceContainer

class SubmissionTupleCompiler @Inject constructor(
    private val logger: Logger,
    private val runtimeJarLoader: RuntimeJarLoader,
    private val commonClassTransformer: CommonClassTransformer,
) {
    fun compile(tuple: GradingSubmissionTuple): CompiledSubmissionTuple {
        val library = runtimeJarLoader.loadCompiled(tuple.library)
        val grader = compileGrader(tuple, library)
        val submission = compileSubmission(tuple, library, grader)

        return CompiledSubmissionTuple(tuple, grader, submission, library)
    }

    private fun compileGrader(tuple: GradingSubmissionTuple, library: RuntimeContainer): GraderJarImpl {
        val commonTransformerApplier = applierOf(commonClassTransformer)

        return tuple.grader.compileTo(
            commonTransformerApplier,
            library,
            "Grader",
            InfoJsonResourceExtractor.Grader
        ) {
            if (errors == 0)
                GraderJarImpl(logger, this, library.runtimeResources)
            else
                throw Exception("Grader compilation failed")
        }
    }

    private fun compileSubmission(
        tuple: GradingSubmissionTuple,
        library: RuntimeContainer,
        grader: GraderJarImpl,
    ): Submission {
        val submissionTransformerApplier = createTransformerApplierFromGraders(grader)
        val submissionFileOverrides = calculateSubmissionFileOverrides(grader)

        // maps assignment ids to files that should be replaced with solution files
        val replacements = submissionFileOverrides.mapValues { (assignmentId, solutionOverrides) ->
            val gradersForAssignment = sequenceOf(grader).filter { it.info.assignmentId == assignmentId }
            solutionOverrides.mapNotNull { solutionOverride ->
                gradersForAssignment.firstNotNullOfOrNull { it.container.source.sourceFiles[solutionOverride] }
            }.associateBy { it.fileName }
        }

        return tuple.submission.compileTo(
            submissionTransformerApplier,
            library,
            "Submission",
            InfoJsonResourceExtractor.Submission,
            replacements,
        ) submissionCompile@{
            val submissionInfo = this.submissionInfo
                ?: throw Exception("Submission container ${info.name} does not have a submission-info.json!")

            JavaSubmission(submissionInfo, this, library.runtimeResources)
        }
    }

    /**
     * Create a transformer applier that selectively applies transformations to
     * submissions only if the grader contains a rubric for it
     */
    private fun createTransformerApplierFromGraders(grader: GraderJar): TransformationApplier {
        fun GraderJar.createApplier(order: ClassTransformerOrder): TransformationApplier =
            configuration.transformers.createApplier(order) { result ->
                result.submissionInfo?.assignmentId == info.assignmentId
            }

        return sequenceOf(
            grader.createApplier(ClassTransformerOrder.PRE),
            applierOf(SubmissionVerificationTransformer(), commonClassTransformer),
            grader.createApplier(ClassTransformerOrder.DEFAULT),
        ).reduce { a, b -> a + b }
    }

    private fun calculateSubmissionFileOverrides(grader: GraderJar): Map<String, List<String>> {
        return grader.configuration.fileNameSolutionOverrides.map { solutionOverride ->
            grader.info.assignmentId to solutionOverride
        }.groupBy({ it.first }, { it.second })
    }

    private fun <T> ResourceContainer.compileTo(
        transformerApplier: TransformationApplier,
        library: RuntimeContainer,
        containerType: String,
        resourceExtractor: ResourceExtractor = ResourceExtractor { _, _, _, _ -> },
        replacements: Map<String, Map<String, JavaSourceFile>> = mapOf(),
        constructor: JavaCompiledContainer.() -> T,
    ): T {
        val originalSources = runtimeJarLoader.loadSources(this, resourceExtractor)
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

        val original = runtimeJarLoader.compileSources(replacedSources, library.runtimeResources)

        val transformed = try {
            val classLoader = RuntimeClassLoaderImpl(original.runtimeResources + library.runtimeResources)
            transformerApplier.transform(original, classLoader)
        } catch (e: Throwable) {
            // create a copy of the original compile result but throw out runtime resources (compiled classes and resources)
            original.copy(
                runtimeResources = RuntimeResources(),
                messages = listOf("Transformation failed :: ${e.message}") + original.messages,
                errors = original.errors + 1,
            )
        }

        return with(transformed) {
            printMessages(
                logger,
                lazyError = { "$containerType ${info.name} has $warnings warnings and $errors errors" },
                lazyWarning = { "$containerType ${info.name} has $warnings warnings" },
            )

            val result = try {
                constructor()
            } catch (e: Exception) {
                logger.error("$containerType container ${info.name} failed to invoke constructor", e)
                throw e
            }

            if (result == null) {
                logger.error("$containerType container ${info.name} failed to load")
            } else {
                logger.info("$containerType container ${info.name} loaded")
            }

            result
        }
    }
}
