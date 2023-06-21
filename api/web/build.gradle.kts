plugins {
    id("kotlin-jvm.base-conventions")
    id("kotlin-js.base-conventions")
    kotlin("plugin.serialization")
}

dependencies {
    commonMainApi(project(":jagr-api-platform"))
    commonMainApi(libs.bundles.ktor.client)
    jsMainApi(libs.ktor.client.js)
    jvmMainApi(libs.ktor.client.cio)
    jvmMainApi(libs.bundles.ktor.server)
}
