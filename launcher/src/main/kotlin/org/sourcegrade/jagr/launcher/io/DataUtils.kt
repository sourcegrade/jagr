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

import com.google.common.io.ByteArrayDataInput
import com.google.common.io.ByteArrayDataOutput
import java.time.Instant
import kotlin.reflect.KClass

fun <T : Any> ByteArrayDataInput.readKClass(): KClass<T> = Class.forName(readUTF()).kotlin as KClass<T>

fun ByteArrayDataOutput.writeKClass(type: KClass<*>) {
  writeUTF(requireNotNull(type.qualifiedName) { "$type must have a qualified name" })
}

fun ByteArrayDataInput.readByteArray(): ByteArray = ByteArray(readInt()) { readByte() }

fun ByteArrayDataOutput.writeByteArray(array: ByteArray) {
  writeInt(array.size)
  write(array)
}

fun ByteArrayDataInput.readInstant(): Instant = Instant.ofEpochSecond(readLong(), readInt().toLong())

fun ByteArrayDataOutput.writeInstant(instant: Instant) {
  writeLong(instant.epochSecond)
  writeInt(instant.nano)
}

fun ByteArrayDataInput.readNull(): Boolean = readByte() == 0.toByte()

fun ByteArrayDataOutput.writeNull() {
  writeByte(0)
}

fun ByteArrayDataOutput.writeNotNull() {
  writeByte(1)
}
