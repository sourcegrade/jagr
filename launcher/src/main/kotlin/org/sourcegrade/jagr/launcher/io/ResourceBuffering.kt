package org.sourcegrade.jagr.launcher.io

fun Resource.buffered(): Resource {
    return buildResource(size) {
        name = this.name
        getInputStream().use { input ->
            repeat(size) {
                outputStream.write(input.read())
            }
        }
    }
}

fun ResourceContainer.buffered(): ResourceContainer {
    return ListResourceContainer(info, resources.map { it.buffered() }.toList())
}
