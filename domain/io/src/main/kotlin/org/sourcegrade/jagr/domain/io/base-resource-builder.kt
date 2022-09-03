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

package org.sourcegrade.jagr.domain.io

import java.io.File
import java.io.InputStream

inline fun buildResourceContainer(configure: ResourceContainer.Builder<Resource>.() -> Unit): ResourceContainer<Resource> =
    createResourceContainerBuilder().apply(configure).build()

inline fun ResourceContainer.Builder<Resource>.addResource(configure: Resource.Builder.() -> Unit) = addResource(buildResource(configure))

fun createResourceContainerBuilder(): ResourceContainer.Builder<Resource> = ResourceContainerBuilderImpl()

fun createResourceContainer(name: String, inputStream: InputStream): ResourceContainer<Resource> {
    // we assume inputStream is in ZIP format
    return ZipResourceContainer(name, inputStream)
}

fun createResourceContainer(file: File): ResourceContainer<Resource> = when (file.extension) {
    "zip",
    "jar",
    -> ZipResourceContainer(file)
    else -> throw IllegalArgumentException("Could not an appropriate resource container for $file")
}



fun ResourceContainer<*>.writeAsDirIn(dir: File) {
    val root = dir.resolve(info.name)
    for (resource in this) {
        resource.writeIn(root)
    }

    foo<_, ResourceContainer<Resource>>()
}

fun <R : Resource, RC : ResourceContainer<R>> foo() {

}

private class ResourceContainerBuilderImpl : ResourceContainer.Builder<Resource> {
    private val resources = mutableMapOf<String, Resource>()
    override lateinit var info: ResourceContainerInfo
    override fun addResource(resource: Resource) = resources.set(resource.name, resource)
    override fun build(): ResourceContainer<Resource> = MapResourceContainer(info, resources)
}

private class MapResourceContainer<R : Resource>(
    override val info: ResourceContainerInfo,
    private val resources: Map<String, R>,
) : IndexedResourceContainer<R> {
    override fun get(name: String): R? = resources[name]
    override fun iterator(): Iterator<R> = resources.values.iterator()
}
