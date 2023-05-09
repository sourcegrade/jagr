package org.sourcegrade.jagr.core.compiler.phisn

import com.google.inject.Inject
import org.apache.logging.log4j.Logger
import org.sourcegrade.jagr.core.compiler.ResourceExtractor
import org.sourcegrade.jagr.core.compiler.RuntimeContainer
import org.sourcegrade.jagr.core.compiler.java.*
import org.sourcegrade.jagr.core.compiler.submissionInfo
import org.sourcegrade.jagr.core.transformer.CommonClassTransformer
import org.sourcegrade.jagr.core.transformer.TransformationApplier
import org.sourcegrade.jagr.launcher.io.ResourceContainer

class ResourceContainerCompiler @Inject constructor(
    private val logger: Logger,
    private val runtimeJarLoader: RuntimeJarLoader,
    private val commonClassTransformer: CommonClassTransformer,
) {
    fun <T> compileTo(
        resourceContainer: ResourceContainer,
        transformerApplier: TransformationApplier,
        library: RuntimeContainer,
        containerType: String,
        resourceExtractor: ResourceExtractor = ResourceExtractor { _, _, _, _ -> },
        replacements: Map<String, Map<String, JavaSourceFile>> = mapOf(),
        constructor: JavaCompiledContainer.() -> T,
    ): T {
        val originalSources = runtimeJarLoader.loadSources(resourceContainer, resourceExtractor)
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
