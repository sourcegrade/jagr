plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

dependencies {
    api(project(":jagr-launcher"))
    implementation(libs.csv)
    implementation(libs.asm.util)
    implementation(libs.serialization)
    implementation(libs.logging.impl)
    implementation(libs.jansi)
    implementation(libs.juniversalchardet)
    implementation(libs.configurate.core)
    implementation(kotlin("reflect"))
    implementation(files("../gradle/wrapper/gradle-wrapper.jar"))
    runtimeOnly(libs.apiguardian)
}
tasks {
    test {
        useJUnitPlatform()
    }
}
