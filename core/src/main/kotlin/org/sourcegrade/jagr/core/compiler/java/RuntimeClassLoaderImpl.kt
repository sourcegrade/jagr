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

import org.sourcegrade.jagr.api.testing.ClassTransformer
import org.sourcegrade.jagr.api.testing.RuntimeClassLoader
import org.sourcegrade.jagr.core.transformer.transform
import org.sourcegrade.jagr.launcher.io.SerializationScope
import org.sourcegrade.jagr.launcher.io.SerializerFactory
import org.sourcegrade.jagr.launcher.io.get
import java.io.InputStream
import java.net.URL
import java.net.URLConnection
import java.net.URLStreamHandler
import java.util.Collections
import java.util.Enumeration

class RuntimeClassLoaderImpl(
    private val runtimeResources: RuntimeResources,
    parent: ClassLoader = RuntimeClassLoaderImpl::class.java.classLoader,
) : ClassLoader(parent), RuntimeClassLoader {

    @Throws(ClassNotFoundException::class, ClassFormatError::class)
    override fun findClass(name: String): Class<*> {
        val compiledClass = runtimeResources.classes[name] ?: return super.findClass(name)
        val byteCode: ByteArray = compiledClass.bytecode
        return defineClass(name, byteCode, 0, byteCode.size)
    }

    override fun findResource(name: String): URL? {
        val resource: ByteArray = runtimeResources.resources[name] ?: return null
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

    override fun findResources(name: String): Enumeration<URL> {
        return Collections.enumeration(listOf(findResource(name) ?: return Collections.emptyEnumeration()))
    }

    override fun getResourceAsStream(name: String): InputStream? {
        return runtimeResources.resources[name]?.inputStream() ?: super.getResourceAsStream(name)
    }

    override fun loadClass(name: String, vararg transformers: ClassTransformer): Class<*> {
        return loadClass(name, transformers.asIterable())
    }

    override fun loadClass(name: String, transformers: Iterable<ClassTransformer>): Class<*> {
        val classStream = getResourceAsStream("${name.replace('.', '/')}.class")
            ?: throw ClassNotFoundException(name)
        var byteCode: ByteArray = classStream.readAllBytes()
        for (transformer in transformers) {
            byteCode = transformer.transform(byteCode, this)
        }
        return defineClass(name, byteCode, 0, byteCode.size)
    }

    override fun getClassNames(): Set<String> = runtimeResources.classes.keys
    override fun getResourceNames(): Set<String> = runtimeResources.resources.keys

    companion object Factory : SerializerFactory<RuntimeClassLoaderImpl> {
        override fun read(scope: SerializationScope.Input) = RuntimeClassLoaderImpl(scope[RuntimeResources::class])

        override fun write(obj: RuntimeClassLoaderImpl, scope: SerializationScope.Output) {
        }
    }
}
