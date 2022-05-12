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
@file:UseSerializers(serializerClasses = [SafeStringSerializer::class])

package org.sourcegrade.jagr.core.testing

import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.sourcegrade.jagr.launcher.io.SerializationScope
import org.sourcegrade.jagr.launcher.io.SerializerFactory
import org.sourcegrade.jagr.launcher.io.SourceSetInfo
import org.sourcegrade.jagr.launcher.io.readList
import org.sourcegrade.jagr.launcher.io.writeList

@Serializable
data class SourceSetInfoImpl(
    override val name: String,
    override val files: List<String>,
) : SourceSetInfo {
    companion object Factory : SerializerFactory<SourceSetInfoImpl> {
        override fun read(scope: SerializationScope.Input) = SourceSetInfoImpl(scope.input.readUTF(), scope.readList())

        override fun write(obj: SourceSetInfoImpl, scope: SerializationScope.Output) {
            scope.output.writeUTF(obj.name)
            scope.writeList(obj.files)
        }
    }
}
