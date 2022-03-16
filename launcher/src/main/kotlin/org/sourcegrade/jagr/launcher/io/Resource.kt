/*
 *   Jagr - SourceGrade.org
 *   Copyright (C) 2021 Alexander Staeding
 *   Copyright (C) 2021 Contributors
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

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream

interface Resource {
    val name: String
    val size: Int
    fun getInputStream(): InputStream
    interface Builder {
        var name: String
        val outputStream: ByteArrayOutputStream
        fun build(): Resource
    }

    companion object {
        const val DEFAULT_SIZE = 2048
    }
}

inline fun buildResource(size: Int = Resource.DEFAULT_SIZE, configure: Resource.Builder.() -> Unit): Resource =
    createResourceBuilder(size).apply(configure).build()

fun createResourceBuilder(size: Int = Resource.DEFAULT_SIZE): Resource.Builder = ResourceBuilderImpl(size)

fun Resource.writeIn(dir: File, name: String? = null): File {
    val file = dir.resolve(name ?: this.name)
    with(file.parentFile) {
        check(exists() || mkdirs()) { "Unable to create directory $this" }
    }
    file.outputStream().buffered().use { getInputStream().copyTo(it) }
    return file
}

private class ResourceBuilderImpl(size: Int) : Resource.Builder {
    override lateinit var name: String
    override val outputStream = ReadableByteArrayOutputStream(size)
    override fun build(): Resource = outputStream.toResource(name)
}

private class ReadableByteArrayOutputStream(size: Int) : ByteArrayOutputStream(size) {
    fun toResource(name: String): Resource = ByteArrayResource(name, buf, count)
}

internal class ByteArrayResource(
    override val name: String,
    private val buf: ByteArray,
    override val size: Int = buf.size,
) : Resource {
    override fun getInputStream(): InputStream = ByteArrayInputStream(buf, 0, size)
}
