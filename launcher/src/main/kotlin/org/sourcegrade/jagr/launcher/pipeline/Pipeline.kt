package org.sourcegrade.jagr.launcher.pipeline

import org.sourcegrade.jagr.launcher.io.GradingBatch

interface Pipeline {
    suspend fun processBatch(batch: GradingBatch, compilationService: CompilationService) {

    }
}
