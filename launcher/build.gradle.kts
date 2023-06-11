import org.sourcegrade.jagr.script.apiProject

plugins {
    kotlin("kapt")
    kotlin("plugin.serialization")
    id("jagr-publish")
    id("jagr-sign")
    id("kotlin-jvm.base-conventions")
}

dependencies {
    apiProject(project, "jagr-api-grader")
    commonMainImplementation(kotlin("reflect"))
    jvmMainApi(libs.coroutines)
    jvmMainImplementation(libs.configurate.hocon)
    jvmMainImplementation(libs.configurate.kotlin)
    jvmMainImplementation(libs.annotations)
    jvmMainImplementation(libs.kotlinx.serialization)
    jvmMainImplementation(libs.logging.core)
    kapt(libs.logging.core)
}

tasks {
    @Suppress("UnstableApiUsage")
    withType<ProcessResources> {
        from(rootProject.file("version")) {
            into("org/sourcegrade/jagr/")
        }
    }
}
