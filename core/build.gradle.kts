plugins {
    kotlin("plugin.serialization")
    id("jagr-publish")
    id("jagr-sign")
    id("kotlin-jvm.base-conventions")
}

dependencies {
    commonMainApi(project(":jagr-api"))
    jvmMainApi(project(":jagr-launcher"))
    jvmMainImplementation(libs.csv)
    jvmMainImplementation(libs.asm.util)
    jvmMainImplementation(libs.kotlinx.serialization)
    jvmMainImplementation(libs.jansi)
    jvmMainImplementation(libs.juniversalchardet)
    jvmMainImplementation(kotlin("reflect"))
    jvmMainImplementation(files("../gradle/wrapper/gradle-wrapper.jar"))
    jvmMainRuntimeOnly(libs.apiguardian)
}
tasks {
    test {
        useJUnitPlatform()
    }
}
