plugins {
    id("kotlin-jvm.base-conventions")
    id("kotlin-js.base-conventions")
}

dependencies {
    commonMainImplementation(project(":jagr-api-platform"))
    jvmMainImplementation(libs.bundles.ktor.client)
    jvmMainImplementation(libs.bundles.ktor.server)
}
