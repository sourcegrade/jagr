package org.sourcegrade.jagr.launcher.compiler.phisn

import org.sourcegrade.jagr.launcher.io.GraderJar
import org.sourcegrade.jagr.launcher.io.ResourceContainer

interface GraderCompiler {
    fun compile(libraryResource: ResourceContainer, graderResource: ResourceContainer): GraderJar
}
