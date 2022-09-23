import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

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
    implementation(
        "org.sourcegrade:submitter:" +
            libs.plugins.submitter.get().toString().substringAfter(':')
    )
    runtimeOnly(project(":jagr-core"))
    implementation(project(":jagr-launcher"))
    implementation(files(rootProject.file("version")))
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "11"
    }
    withType<JavaCompile> {
        options.encoding = "UTF-8"
        sourceCompatibility = "11"
        targetCompatibility = "11"
    }
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
    vcsUrl = "https://github.com/sourcegrade/jagr-gradle"
    tags = listOf("jagr", "assignment", "submission", "grading")
}
