plugins {
    `java-gradle-plugin`
    kotlin("plugin.serialization")
    id("jagr-publish")
    // signing done by gradle-publish plugin
    id("kotlin-jvm.base-conventions")
}

tasks {
    withType<PublishToMavenRepository> {
        onlyIf { project.version.toString().endsWith("-SNAPSHOT") }
    }
}

dependencies {
    jvmMainCompileOnly(gradleKotlinDsl())
    jvmMainImplementation(libs.kotlinx.serialization)
    jvmMainImplementation(libs.logging.core)
    jvmMainImplementation("de.undercouch:gradle-download-task:${libs.plugins.download.get().version}")
    jvmMainRuntimeOnly(project(":jagr-core"))
    jvmMainImplementation(project(":jagr-launcher"))
}

gradlePlugin {
    plugins {
        register("jagr-gradle") {
            id = "org.sourcegrade.jagr-gradle"
            displayName = "Jagr Gradle Plugin"
            description = "Gradle plugin for running the Jagr AutoGrader"
            implementationClass = "org.sourcegrade.jagr.gradle.JagrGradlePlugin"
            tags.set(listOf("jagr", "assignment", "submission", "grading"))
        }
    }
    website.set("https://www.sourcegrade.org")
    vcsUrl.set("https://github.com/sourcegrade/jagr")
}
