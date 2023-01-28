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

package org.sourcegrade.jagr.gradle.extension

import com.google.inject.Inject
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.property
import org.sourcegrade.jagr.launcher.env.Jagr

abstract class JagrDownloadExtension @Inject constructor(
    objectFactory: ObjectFactory,
) {
    val jagrVersion: Property<String> = objectFactory.property<String>()
        .convention(Jagr.version)
    val sourceUrl: Property<String> = objectFactory.property<String>()
        .convention(jagrVersion.map { "https://github.com/sourcegrade/jagr/releases/download/v$it/Jagr-$it.jar" })
    val destName: Property<String> = objectFactory.property<String>()
        .convention(jagrVersion.map { "Jagr-$it.jar" })
}
