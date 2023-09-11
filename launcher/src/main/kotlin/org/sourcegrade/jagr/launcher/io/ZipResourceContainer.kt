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
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

internal class ZipResourceContainer(
    override val info: ResourceContainerInfo,
    private val input: InputStream,
) : ResourceContainer {
    constructor(name: String, input: InputStream) : this(ResourceContainerInfoImpl(name), input)
    constructor(file: File) : this(file.name, file.inputStream().buffered())

    override fun iterator(): Iterator<Resource> = ZipResourceIterator(ZipInputStream(input))
    override fun toString(): String = info.toString()
}

private class ZipResourceIterator(private val zip: ZipInputStream) : Iterator<Resource> {
    private var next: ZipEntry? = null

    private fun calculateNext(): ZipEntry? {
        // skip directory entries
        do {
            next = zip.nextEntry
        } while (next?.isDirectory == true)
        return next
    }

    override fun hasNext(): Boolean {
        if (next == null) {
            calculateNext()
            return next != null
        }
        return true
    }

    override fun next(): Resource {
        return next?.let {
            // return cached next from hasNext() and reset it
            ByteArrayResource(it.name, zip.readBytes()).also { next = null }
        } // no cached next, calculate and return
            ?: ByteArrayResource(requireNotNull(calculateNext()) { "No next entry!" }.name, zip.readBytes())
    }
}
