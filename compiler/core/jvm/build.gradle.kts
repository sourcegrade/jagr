import org.sourcegrade.jagr.script.apiProjects

plugins {
    kotlin("jvm")
}

dependencies {
    apiProjects(version, "jagr-compiler-api-jvm")
    apiProjects(version, "jagr-compiler-core")
    implementation(libs.annotations)
}
