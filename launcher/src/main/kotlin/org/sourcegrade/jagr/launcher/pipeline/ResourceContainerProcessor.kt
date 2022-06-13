package org.sourcegrade.jagr.launcher.pipeline

import org.sourcegrade.jagr.launcher.io.ResourceContainer

interface ResourceContainerProcessor<T : ProcessedContainer> {
    fun load(container: ResourceContainer): T
}

