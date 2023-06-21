plugins {
    id("kotlin-jvm.base-conventions")
    id("kotlin-js.base-conventions")
    kotlin("plugin.serialization")
}

dependencies {
    commonMainApi(project(":jagr-api-web"))
}
