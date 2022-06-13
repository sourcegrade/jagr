package org.sourcegrade.jagr.core.compiler.jvm

import org.sourcegrade.jagr.launcher.pipeline.ResourceCollectorImpl
import org.sourcegrade.jagr.launcher.pipeline.ResourceContainerProcessor
import org.sourcegrade.jagr.core.compiler.java.CompiledClass
import org.sourcegrade.jagr.core.compiler.java.JavaRuntimeContainer
import org.sourcegrade.jagr.core.compiler.java.RuntimeResources
import org.sourcegrade.jagr.launcher.io.ResourceContainer

class JVMCompiledResourceContainerProcessor : ResourceContainerProcessor {
    override fun load(container: ResourceContainer) {
        val resourceCollector = ResourceCollectorImpl()
        val classStorage: MutableMap<String, CompiledClass> = mutableMapOf()
        val resources: MutableMap<String, ByteArray> = mutableMapOf()
        for (resource in container) {
            when {
                resource.name.endsWith(".class") -> {
                    val className = resource.name.replace('/', '.').substring(0, resource.name.length - 6)
                    classStorage[className] = CompiledClass.Existing(className, resource.getInputStream().use { it.readBytes() })
                }
                resource.name.endsWith("MANIFEST.MF") -> { // ignore
                }
                else -> resources[resource.name] = resource.getInputStream().use { it.readAllBytes() }
                    .also { data -> resourceExtractor.extract(container.info, resource, data, resourceCollector) }
            }
        }
        return JavaRuntimeContainer(container.info, resourceCollector, RuntimeResources(classStorage, resources))
    }
}
