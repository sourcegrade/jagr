

@Suppress("DSL_SCOPE_VIOLATION") // https://youtrack.jetbrains.com/issue/KTIJ-19369
plugins {
    `java-gradle-plugin`
    alias(libs.plugins.gradle.publish)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    implementation(gradleKotlinDsl())
    implementation(libs.serialization)
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
