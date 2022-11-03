import org.sourcegrade.jagr.script.JagrPublishPlugin

@Suppress("DSL_SCOPE_VIOLATION") // https://youtrack.jetbrains.com/issue/KTIJ-19369
plugins {
    `java-gradle-plugin`
    alias(libs.plugins.gradle.publish)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
}

apply<JagrPublishPlugin>()

tasks {
    withType<PublishToMavenRepository> {
        onlyIf { project.version.toString().endsWith("-SNAPSHOT") }
    }
}

dependencies {
    implementation(gradleKotlinDsl())
    implementation(libs.serialization)
    implementation(libs.logging.core)
    runtimeOnly(project(":jagr-core"))
    implementation(project(":jagr-launcher"))
}

gradlePlugin {
    plugins {
        create("jagr-gradle") {
            id = "org.sourcegrade.jagr-gradle"
            displayName = "Jagr Gradle Plugin"
            description = "Gradle plugin for running the Jagr AutoGrader"
            implementationClass = "org.sourcegrade.jagr.gradle.JagrGradlePlugin"
        }
    }
}

pluginBundle {
    website = "https://www.sourcegrade.org"
    vcsUrl = "https://github.com/sourcegrade/jagr"
    tags = listOf("jagr", "assignment", "submission", "grading")
}
