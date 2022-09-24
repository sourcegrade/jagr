import org.sourcegrade.jagr.script.apiProject

plugins {
    kotlin("jvm")
    kotlin("kapt")
    kotlin("plugin.serialization")
    id("com.github.johnrengelman.shadow")
}

dependencies {
    apiProject(project, "jagr-grader-api")
    api(libs.coroutines)
    implementation(libs.configurate.hocon)
    implementation(libs.annotations)
    implementation(libs.serialization)
    implementation(libs.logging.impl)
    implementation(libs.logging.core)
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
