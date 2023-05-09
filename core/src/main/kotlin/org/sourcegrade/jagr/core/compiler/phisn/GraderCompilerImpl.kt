package org.sourcegrade.jagr

import com.google.inject.Inject
import org.apache.logging.log4j.Logger
import org.sourcegrade.jagr.core.compiler.InfoJsonResourceExtractor
import org.sourcegrade.jagr.core.compiler.RuntimeContainer
import org.sourcegrade.jagr.core.compiler.java.*
import org.sourcegrade.jagr.core.compiler.phisn.ResourceContainerCompiler
import org.sourcegrade.jagr.core.testing.GraderJarImpl
import org.sourcegrade.jagr.core.transformer.*
import org.sourcegrade.jagr.launcher.io.GraderJar
import org.sourcegrade.jagr.launcher.io.ResourceContainer
import org.sourcegrade.jagr.launcher.compiler.phisn.GraderCompiler

class GraderCompilerImpl @Inject constructor(
    private val logger: Logger,
    private val runtimeJarLoader: RuntimeJarLoader,
    private val commonClassTransformer: CommonClassTransformer,
    private val resourceContainerCompiler: ResourceContainerCompiler
) : GraderCompiler {
    override fun compile(libraryResource: ResourceContainer, graderResource: ResourceContainer): GraderJar {
        val library = runtimeJarLoader.loadCompiled(libraryResource)
        return compileGrader(graderResource, library)
    }

    private fun compileGrader(grader: ResourceContainer, library: RuntimeContainer): GraderJar {
        val commonTransformerApplier = applierOf(commonClassTransformer)

        return resourceContainerCompiler.compileTo(
            grader,
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
}
