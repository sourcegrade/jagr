/*
 *   Jagr - SourceGrade.org
 *   Copyright (C) 2021-2022 Alexander Staeding
 *   Copyright (C) 2021-2022 Contributors
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.sourcegrade.jagr.gradle

import org.gradle.api.tasks.SourceSet

internal fun SourceSet.forEachFile(action: (directorySet: String, fileName: String) -> Unit) {
    for (directorySet in allSource.sourceDirectories) {
        for (file in directorySet.walkTopDown()) {
            if (file.isFile) {
                action(directorySet.name, file.relativeTo(directorySet).invariantSeparatorsPath)
            }
        }
    }
}

internal fun SourceSet.getFiles(): Map<String, Set<String>> {
    val result = mutableMapOf<String, MutableSet<String>>()
    forEachFile { directorySet, fileName -> result.computeIfAbsent(directorySet) { mutableSetOf() }.add(fileName) }
    return result
}

fun List<SourceSet>.mergeSourceSets(): Map<String, Map<String, Set<String>>> {
    return asSequence()
        .map { it.name to it.getFiles() }
        .fold(mutableMapOf()) { acc, (sourceSetName, sourceSetDir) ->
            acc.merge(sourceSetName, sourceSetDir) { a, b ->
                (a.asSequence() + b.asSequence()).fold(mutableMapOf()) { map, (name, files) ->
                    map.merge(name, files) { x, y -> x + y }
                    map
                }
            }
            acc
        }
}
