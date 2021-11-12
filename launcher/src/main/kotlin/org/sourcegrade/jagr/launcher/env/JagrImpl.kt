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

package org.sourcegrade.jagr.launcher.env

import com.google.inject.Guice
import com.google.inject.Injector
import kotlinx.serialization.Serializable
import kotlin.reflect.full.primaryConstructor

private data class JagrImpl(override val injector: Injector) : Jagr

@Serializable
data class JagrJson(
  /**
   * guice modules to bind
   */
  val moduleFactories: List<String>,
)

fun JagrJson.toJagr(configuration: LaunchConfiguration): Jagr {
  val modules = moduleFactories.map {
    coerceClass<ModuleFactory>(it).kotlin.run {
      (objectInstance ?: primaryConstructor!!.call()).create(configuration)
    }
  }.toTypedArray()
  val injector = Guice.createInjector(*modules)
  return JagrImpl(injector)
}

private inline fun <reified T : Any> coerceClass(implementation: String): Class<out T> {
  return Class.forName(implementation).asSubclass(T::class.java)
}
