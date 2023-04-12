package org.sourcegrade.jagr.gradle.extension

import org.gradle.api.Project
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.kotlin.dsl.getByType
import java.io.Serializable

data class ProjectSourceSetTuple(
    val projectPath: String,
    val sourceSetName: String,
) : Serializable {
    companion object {
        fun fromSourceSetNames(projectPath: String, sourceSetNames: Sequence<String>) =
            sourceSetNames.map { ProjectSourceSetTuple(projectPath, it) }.toSet()

        fun fromSourceSetNames(projectPath: String, sourceSetNames: Iterable<String>) =
            fromSourceSetNames(projectPath, sourceSetNames.asSequence())
    }
}

fun ProjectSourceSetTuple.getSourceSet(rootProject: Project): SourceSet =
    rootProject.relative(projectPath).extensions.getByType<SourceSetContainer>().getByName(sourceSetName)
