import org.sourcegrade.jagr.script.apiProjects

plugins {
    kotlin("jvm")
}

dependencies {
    apiProjects(version, "jagr-compiler-api-java")
    apiProjects(version, "jagr-compiler-core-jvm")
    implementation(libs.annotations)
}
