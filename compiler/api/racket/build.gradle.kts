import org.sourcegrade.jagr.script.apiProjects

plugins {
    kotlin("jvm")
}

dependencies {
    apiProjects(version, "jagr-compiler-api")
    implementation(libs.annotations)
}
