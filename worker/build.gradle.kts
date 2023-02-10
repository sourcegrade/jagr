import org.sourcegrade.jagr.script.JagrPublishPlugin
import org.sourcegrade.jagr.script.apiProject

plugins {
    kotlin("jvm")
    kotlin("kapt")
    kotlin("plugin.serialization")
    id("com.github.johnrengelman.shadow")
}

apply<JagrPublishPlugin>()

dependencies {
    apiProject(project, "jagr-grader-api")
    api(libs.coroutines)
    implementation(libs.configurate.hocon)
    implementation(libs.configurate.kotlin)
    implementation(libs.annotations)
    implementation(libs.serialization)
    implementation(libs.logging.core)
    implementation(project(":jagr-launcher"))
    implementation(project(":jagr-core"))
    kapt(libs.logging.core)
    implementation(kotlin("reflect"))
}

tasks {
    @Suppress("UnstableApiUsage")
    withType<ProcessResources> {
        from(rootProject.file("version")) {
            into("org/sourcegrade/jagr/")
        }
    }
}
