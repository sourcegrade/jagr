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

import java.io.DataInput
import java.io.DataOutput
import java.time.Instant
import kotlin.reflect.KClass

fun <T : Any> DataInput.readKClass(): KClass<T> = Class.forName(readUTF()).kotlin as KClass<T>

fun DataOutput.writeKClass(type: KClass<*>) {
    writeUTF(requireNotNull(type.qualifiedName) { "$type must have a qualified name" })
}

fun DataInput.readByteArray(): ByteArray = ByteArray(readInt()) { readByte() }

fun DataOutput.writeByteArray(array: ByteArray) {
    writeInt(array.size)
    write(array)
}

fun DataInput.readInstant(): Instant = Instant.ofEpochSecond(readLong(), readInt().toLong())

fun DataOutput.writeInstant(instant: Instant) {
    writeLong(instant.epochSecond)
    writeInt(instant.nano)
}

fun DataInput.readNull(): Boolean = readByte() == 0.toByte()

fun DataOutput.writeNull() {
    writeByte(0)
}

fun DataOutput.writeNotNull() {
    writeByte(1)
}
