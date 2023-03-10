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

package org.sourcegrade.jagr.launcher.io

import java.io.File
import java.io.InputStream
import java.util.Base64

interface ResourceContainer : Sequence<Resource> {
    val info: ResourceContainerInfo

    interface Builder {
        var info: ResourceContainerInfo
        fun addResource(resource: Resource)
        fun build(): ResourceContainer
    }
}

interface ResourceContainerInfo {
    val name: String

    interface Builder {
        var name: String
        fun build(): ResourceContainerInfo
    }

    companion object Factory : SerializerFactory<ResourceContainerInfo> {
        override fun read(scope: SerializationScope.Input): ResourceContainerInfo =
            ResourceContainerInfoImpl(scope.input.readUTF())

        override fun write(obj: ResourceContainerInfo, scope: SerializationScope.Output) =
            scope.output.writeUTF(obj.name)
    }
}

internal data class ResourceContainerInfoImpl(override val name: String) : ResourceContainerInfo

val ResourceContainerInfo.nameWithoutExtension: String
    get() = name.substringBeforeLast(".")

fun Resource.toContainer(): ResourceContainer = createResourceContainer(name, getInputStream())

inline fun buildResourceContainer(configure: ResourceContainer.Builder.() -> Unit): ResourceContainer =
    createResourceContainerBuilder().apply(configure).build()

inline fun ResourceContainer.Builder.addResource(configure: Resource.Builder.() -> Unit) = addResource(buildResource(configure))

fun createResourceContainerBuilder(): ResourceContainer.Builder = ResourceContainerBuilderImpl()

inline fun buildResourceContainerInfo(configure: ResourceContainerInfo.Builder.() -> Unit): ResourceContainerInfo =
    createResourceContainerInfoBuilder().apply(configure).build()

fun createResourceContainerInfoBuilder(): ResourceContainerInfo.Builder = ResourceContainerInfoBuilderImpl()

fun createResourceContainer(name: String, inputStream: InputStream): ResourceContainer {
    // we assume inputStream is in ZIP format
    return ZipResourceContainer(name, inputStream)
}

fun createResourceContainer(file: File): ResourceContainer = when (file.extension) {
    "zip",
    "jar",
    -> ZipResourceContainer(file)
    else -> throw IllegalArgumentException("Could not an appropriate resource container for $file")
}

fun createResourceContainerFromZipBase64(name: String, base64: String): ResourceContainer {
    return createResourceContainer(
        name,
        Base64.getDecoder().wrap(base64.byteInputStream())
    )
}

fun ResourceContainer.writeAsDirIn(dir: File) {
    val root = dir.resolve(info.name)
    for (resource in this) {
        resource.writeIn(root)
    }
}

private class ResourceContainerBuilderImpl : ResourceContainer.Builder {
    private val resources = mutableListOf<Resource>()
    override lateinit var info: ResourceContainerInfo
    override fun addResource(resource: Resource) {
        resources += resource
    }

    override fun build(): ResourceContainer = ListResourceContainer(info, resources)
}

private class ResourceContainerInfoBuilderImpl : ResourceContainerInfo.Builder {
    override lateinit var name: String
    override fun build(): ResourceContainerInfo = ResourceContainerInfoImpl(name)
}

private class ListResourceContainer(
    override val info: ResourceContainerInfo,
    private val resources: List<Resource>,
) : ResourceContainer {
    override fun iterator(): Iterator<Resource> = resources.iterator()
}
