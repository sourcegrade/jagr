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

package org.sourcegrade.jagr.core.compiler.java

import com.google.common.collect.FluentIterable
import com.google.common.reflect.ClassPath
import javax.tools.FileObject
import javax.tools.ForwardingJavaFileManager
import javax.tools.JavaFileManager
import javax.tools.JavaFileObject

class ExtendedStandardJavaFileManager(
    javaFileManager: JavaFileManager,
    private val classPath: Map<String, CompiledClass>,
    private val classStorage: MutableMap<String, CompiledClass>,
) : ForwardingJavaFileManager<JavaFileManager>(javaFileManager) {

    override fun getJavaFileForOutput(
        location: JavaFileManager.Location,
        className: String,
        kind: JavaFileObject.Kind,
        sibling: FileObject,
    ): JavaFileObject {
        try {
            val compiledClass = CompiledClass.Runtime(className)
            classStorage[compiledClass.name] = compiledClass
            return compiledClass
        } catch (e: Exception) {
            throw IllegalStateException("Error creating class file for $className", e)
        }
    }

    override fun list(
        location: JavaFileManager.Location?,
        packageName: String?,
        kinds: MutableSet<JavaFileObject.Kind>,
        recurse: Boolean,
    ): Iterable<JavaFileObject> {
        val fromDelegate = super.list(location, packageName, kinds, recurse)
        if (packageName != null && kinds.contains(JavaFileObject.Kind.CLASS) && location != null && location.name == "CLASS_PATH") {
            return if (packageName.startsWith("org.sourcegrade")) {
                getCompiledClassesFromClassPath(packageName) + searchClassLoader(packageName)
            } else {
                fromDelegate + getCompiledClassesFromClassPath(packageName)
            }
        }
        return fromDelegate
    }

    private fun getCompiledClassesFromClassPath(packageName: String): List<JavaFileObject> {
        return classPath.asSequence()
            .filter { it.key.startsWith(packageName) }
            .map { it.value }
            .toList()
    }

    @Suppress("UnstableApiUsage")
    private fun searchClassLoader(packageName: String): List<JavaFileObject> {
        val classLoader = javaClass.classLoader
        return FluentIterable.from(ClassPath.from(classLoader).resources)
            .mapNotNull { it as? ClassPath.ClassInfo }
            .filter { it.packageName == packageName }
            .map {
                val className = "${it.name.replace('.', '/')}.class"
                val classStream = checkNotNull(classLoader.getResourceAsStream(className)) {
                    "Could not find class $className in classpath"
                }
                CompiledClass.Existing(it.name, classStream.readAllBytes())
            }.toList()
    }

    override fun inferBinaryName(location: JavaFileManager.Location, file: JavaFileObject): String {
        if (file is CompiledClass.Existing) {
            return file.className
        }
        return super.inferBinaryName(location, file)
    }
}
