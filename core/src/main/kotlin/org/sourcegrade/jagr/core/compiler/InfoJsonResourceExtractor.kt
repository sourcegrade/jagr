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

package org.sourcegrade.jagr.core.compiler

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.serializer
import org.sourcegrade.jagr.launcher.env.Jagr
import org.sourcegrade.jagr.launcher.env.logger
import org.sourcegrade.jagr.launcher.io.Resource
import org.sourcegrade.jagr.launcher.io.ResourceContainerInfo
import java.io.ByteArrayInputStream
import kotlin.reflect.KClass
import kotlin.reflect.full.createType

class InfoJsonResourceExtractor<T : Any>(
    private val type: KClass<T>,
    private val targetName: String,
) : ResourceExtractor {
    companion object {
        inline operator fun <reified T : Any> invoke(targetName: String) = InfoJsonResourceExtractor(T::class, targetName)
    }

    override fun extract(
        containerInfo: ResourceContainerInfo,
        resource: Resource,
        data: ByteArray,
        collector: MutableResourceCollector,
    ) {
        if (resource.name == targetName) {
            try {
                @Suppress("UNCHECKED_CAST")
                val serializer = Json.serializersModule.serializer(type.createType()) as KSerializer<T>
                @OptIn(ExperimentalSerializationApi::class)
                collector.addResource(Json.decodeFromStream(serializer, ByteArrayInputStream(data)))
            } catch (e: Exception) {
                Jagr.logger.error("${containerInfo.name} has invalid $targetName", e)
            }
        }
    }
}
