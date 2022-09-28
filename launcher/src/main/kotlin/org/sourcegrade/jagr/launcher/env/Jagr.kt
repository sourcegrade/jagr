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

package org.sourcegrade.jagr.launcher.env

import com.google.inject.Injector
import org.apache.logging.log4j.Logger
import org.sourcegrade.jagr.launcher.executor.GradingQueue
import org.sourcegrade.jagr.launcher.executor.RuntimeGrader
import org.sourcegrade.jagr.launcher.io.ExtrasManager
import org.sourcegrade.jagr.launcher.io.SerializerFactory
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KClass

interface Jagr {
    val injector: Injector

    companion object Default : Jagr by SystemResourceJagrFactory.create() {
        private const val VERSION_FILE = "org/sourcegrade/jagr/version"
        val version: String by lazy {
            checkNotNull(Jagr::class.java.classLoader.getResourceAsStream(VERSION_FILE)) { "Could not find version file $VERSION_FILE" }
                .bufferedReader().readLine()
        }
    }

    interface Factory {
        fun create(configuration: LaunchConfiguration = LaunchConfiguration.Standard): Jagr
    }
}

val Jagr.config: Config by injected()
val Jagr.extrasManager: ExtrasManager by injected()
val Jagr.gradingQueueFactory: GradingQueue.Factory by injected()
val Jagr.logger: Logger by injected()
val Jagr.runtimeGrader: RuntimeGrader by injected()
val Jagr.serializerFactoryLocator: SerializerFactory.Locator by injected()

internal operator fun <T : Any> Jagr.get(type: KClass<T>): T = injector.getInstance(type.java)
internal inline fun <reified T : Any> Jagr.get(): T = get(T::class)
private inline fun <reified T : Any> injected(): ReadOnlyProperty<Jagr, T> = ReadOnlyProperty { e, _ -> e.get() }
