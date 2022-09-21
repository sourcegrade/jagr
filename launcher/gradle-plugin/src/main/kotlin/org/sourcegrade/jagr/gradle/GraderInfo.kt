package org.sourcegrade.jagr.gradle

import kotlinx.serialization.Serializable
import org.gradle.api.tasks.SourceSet

@Serializable
internal data class GraderInfo(
    val name: String,
    val assignmentId: String,
    val sourceDescriptors: Map<String, List<String>>,
    val sourceSets: List<SourceSetInfo>,
)

@Serializable
internal data class SourceSetInfo(
    val name: String,
    val files: List<String>,
) : java.io.Serializable

internal fun SourceSet.getFiles(): List<String> {
    return allSource.files.map { file ->
        allSource.srcDirs.asSequence()
            .map(file::relativeTo)
            .reduce { a, b -> if (a.path.length < b.path.length) a else b }
            .path
    }
}

internal fun SourceSet.toInfo() = SourceSetInfo(name, getFiles())
