plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}
repositories {
    maven("https://repo.spongepowered.org/repository/maven-public/")
}

dependencies {
    api(project(":jagr-grader-api"))
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
