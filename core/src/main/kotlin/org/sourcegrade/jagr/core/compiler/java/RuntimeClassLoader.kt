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

package org.sourcegrade.jagr.core.compiler.java

import org.sourcegrade.jagr.launcher.io.SerializationScope
import org.sourcegrade.jagr.launcher.io.SerializerFactory
import org.sourcegrade.jagr.launcher.io.get
import java.io.InputStream
import java.net.URL
import java.net.URLConnection
import java.net.URLStreamHandler

class RuntimeClassLoader(
    private val runtimeResources: RuntimeResources,
    parent: ClassLoader = getSystemClassLoader(),
) : ClassLoader(parent) {

    @Throws(ClassNotFoundException::class, ClassFormatError::class)
    override fun findClass(name: String): Class<*> {
        val compiledClass = runtimeResources.classes[name] ?: return super.findClass(name)
        val byteCode: ByteArray = compiledClass.bytecode
        return defineClass(name, byteCode, 0, byteCode.size)
    }

    override fun getResourceAsStream(name: String): InputStream? {
        return runtimeResources.resources[name]?.inputStream() ?: super.getResourceAsStream(name)
    }

    override fun getResource(name: String?): URL? {
        val resource: ByteArray = runtimeResources.resources[name] ?: return super.getResource(name)
        return URL(
            null,
            "jagrresource:$name",
            object : URLStreamHandler() {
                override fun openConnection(u: URL?): URLConnection {
                    return object : URLConnection(u) {
                        override fun connect() = Unit
                        override fun getInputStream(): InputStream = resource.inputStream()
                    }
                }
            }
        )
    }

    companion object Factory : SerializerFactory<RuntimeClassLoader> {
        override fun read(scope: SerializationScope.Input) = RuntimeClassLoader(scope[RuntimeResources::class])

        override fun write(obj: RuntimeClassLoader, scope: SerializationScope.Output) {
        }
    }
}
