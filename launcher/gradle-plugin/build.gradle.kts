@Suppress("DSL_SCOPE_VIOLATION") // https://youtrack.jetbrains.com/issue/KTIJ-19369
plugins {
    `java-gradle-plugin`
    alias(libs.plugins.gradle.publish)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    id("jagr-publish")
    // signing done by gradle-publish plugin
}

tasks {
    withType<PublishToMavenRepository> {
        onlyIf { project.version.toString().endsWith("-SNAPSHOT") }
    }
}

dependencies {
    implementation(gradleKotlinDsl())
    implementation(libs.serialization)
    implementation(libs.logging.core)
    implementation("de.undercouch:gradle-download-task:${libs.plugins.download.get().version}")
    runtimeOnly(project(":jagr-core"))
    implementation(project(":jagr-launcher"))
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
