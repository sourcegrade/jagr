package org.sourcegrade.jagr.launcher.pipeline

import org.sourcegrade.jagr.launcher.io.ResourceContainer

interface PreProcessor {

    fun process(container: ResourceContainer): Result

    interface Result {
        val language: String
    }
}
