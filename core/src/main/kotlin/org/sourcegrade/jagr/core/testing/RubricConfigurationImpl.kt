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

package org.sourcegrade.jagr.core.testing

import org.sourcegrade.jagr.api.testing.ClassTransformer
import org.sourcegrade.jagr.api.testing.ClassTransformerOrder
import org.sourcegrade.jagr.api.testing.RubricConfiguration
import java.util.Collections

class RubricConfigurationImpl : RubricConfiguration {
    private val transformers = mutableMapOf<ClassTransformerOrder, MutableList<ClassTransformer>>()
    private val fileNameSolutionOverrides = mutableListOf<String>()
    private var exportBuildScriptPath: String? = null
    override fun getTransformers(): Map<ClassTransformerOrder, List<ClassTransformer>> =
        transformers.asSequence().map { (a, b) -> a to Collections.unmodifiableList(b) }.toMap()

    override fun getFileNameSolutionOverrides(): List<String> = Collections.unmodifiableList(fileNameSolutionOverrides)

    override fun getExportBuildScriptPath() = exportBuildScriptPath

    override fun addTransformer(transformer: ClassTransformer, order: ClassTransformerOrder): RubricConfiguration {
        transformers.computeIfAbsent(order) { mutableListOf() } += transformer
        return this
    }

    override fun addFileNameSolutionOverride(fileName: String): RubricConfiguration {
        fileNameSolutionOverrides += fileName
        return this
    }

    override fun setExportBuildScriptPath(path: String?): RubricConfiguration {
        exportBuildScriptPath = path
        return this
    }
}
