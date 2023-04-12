plugins {
    kotlin("plugin.serialization")
    id("jagr-publish")
    id("jagr-sign")
    id("kotlin-jvm.base-conventions")
}

dependencies {
    jvmMainApi(project(":jagr-launcher"))
    jvmMainImplementation(libs.csv)
    jvmMainImplementation(libs.asm.util)
    jvmMainImplementation(libs.serialization)
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
