import org.sourcegrade.jagr.script.apiProjects
import org.sourcegrade.jagr.script.runtimeOnlyProjects

plugins {
    kotlin("jvm")
    kotlin("kapt")
    kotlin("plugin.serialization")
    id("com.github.johnrengelman.shadow")
}

dependencies {
    apiProjects(version, "jagr-domain", "jagr-grader")
    runtimeOnlyProjects(version, "jagr-core")
    implementation(libs.configurate.hocon)
    implementation(libs.annotations)
    implementation(libs.serialization)
    implementation(libs.logging.impl)
    implementation(libs.logging.core)
    kapt(libs.logging.core)
    implementation(kotlin("reflect"))
}
