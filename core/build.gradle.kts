plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("jagr-publish")
    id("jagr-sign")
}

dependencies {
    api(project(":jagr-launcher"))
    implementation(libs.csv)
    implementation(libs.asm.util)
    implementation(libs.serialization)
    implementation(libs.jansi)
    implementation(libs.juniversalchardet)
    implementation(kotlin("reflect"))
    implementation(files("../gradle/wrapper/gradle-wrapper.jar"))
    runtimeOnly(libs.apiguardian)
}
tasks {
    test {
        useJUnitPlatform()
    }
}
