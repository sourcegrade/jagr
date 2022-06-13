package org.sourcegrade.jagr.core.compiler.base

import org.sourcegrade.jagr.core.compiler.jvm.JVMCompilerContainer
import org.sourcegrade.jagr.launcher.io.ResourceContainer

interface CompilationService {

    suspend fun loadCompiled(container: ResourceContainer): JVMCompilerContainer
}
