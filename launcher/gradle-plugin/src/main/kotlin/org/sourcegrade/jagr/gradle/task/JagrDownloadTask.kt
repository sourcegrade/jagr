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

package org.sourcegrade.jagr.gradle.task

import de.undercouch.gradle.tasks.download.Download
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.pathString

@Suppress("LeakingThis")
abstract class JagrDownloadTask : Download() {

    @get:Input
    abstract val sourceUrl: Property<String>

    @get:Input
    abstract val destName: Property<String>

    init {
        group = "jagr resources"
        src(sourceUrl)
        dest(destName.map { JAGR_CACHE.resolve(it).pathString })
        overwrite(false)
    }

    companion object {
        internal val JAGR_HOME: Path = Path(System.getProperty("user.home")).resolve(".jagr").createDirectories()
        internal val JAGR_CACHE: Path = JAGR_HOME.resolve("cache").createDirectories()
    }
}
