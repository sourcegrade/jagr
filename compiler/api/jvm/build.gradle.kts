import org.sourcegrade.jagr.script.apiProjects

plugins {
    kotlin("jvm")
}

dependencies {
    apiProjects(version, "jagr-compiler-api")
    api(libs.asm.core)
    implementation(libs.annotations)
}
