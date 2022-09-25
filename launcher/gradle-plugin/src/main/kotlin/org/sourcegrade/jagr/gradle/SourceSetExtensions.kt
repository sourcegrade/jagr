package org.sourcegrade.jagr.gradle

import org.gradle.api.tasks.SourceSet

internal fun SourceSet.forEachFile(action: (String) -> Unit) {
    return allSource.files.forEach { file ->
        allSource.srcDirs.asSequence()
            .map(file::relativeTo)
            .reduce { a, b -> if (a.path.length < b.path.length) a else b }
            .path
            .apply(action)
    }
}

internal fun SourceSet.getFiles(): List<String> {
    val result = mutableListOf<String>()
    forEachFile { result.add(it) }
    return result
}
