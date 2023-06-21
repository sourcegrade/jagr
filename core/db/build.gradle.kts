plugins {
    id("kotlin-jvm.base-conventions")
}

dependencies {
    commonMainApi(project(":jagr-core"))
    jvmMainApi(platform(libs.exposed.bom))
    jvmMainApi(libs.bundles.exposed)
}
