import com.github.jengelman.gradle.plugins.shadow.transformers.Log4j2PluginsCacheFileTransformer
import org.sourcegrade.jagr.script.apiProject

plugins {
    application
    kotlin("jvm")
    kotlin("kapt")
    kotlin("plugin.serialization")
    id("com.github.johnrengelman.shadow")
    id("jagr-publish")
    id("jagr-sign")
}

dependencies {
    apiProject(project, "jagr-grader-api")
    api(libs.coroutines)
    implementation(libs.configurate.hocon)
    implementation(libs.configurate.kotlin)
    implementation(libs.annotations)
    implementation(libs.serialization)
    implementation(libs.logging.core)
    implementation(project(":jagr-launcher"))
    implementation(project(":jagr-core"))
    kapt(libs.logging.core)
    implementation(kotlin("reflect"))
}

application {
    mainClass.set("org.sourcegrade.jagr.MainKt")
}

tasks {
    val runDir = File("build/run")
    named<JavaExec>("run") {
        doFirst {
            error("Use runShadow instead")
        }
    }
    named<JavaExec>("runShadow") {
        doFirst {
            runDir.mkdirs()
        }
        workingDir = runDir
    }
    jar {
        enabled = false
    }
    shadowJar {
        transform(Log4j2PluginsCacheFileTransformer::class.java)
        from("gradlew") {
            into("org/gradle")
        }
        from("gradlew.bat") {
            into("org/gradle")
        }
        from("gradle/wrapper/gradle-wrapper.properties") {
            into("org/gradle")
        }
        archiveFileName.set("Jagr-${project.version}.jar")
    }
}


tasks {
    @Suppress("UnstableApiUsage")
    withType<ProcessResources> {
        from(rootProject.file("version")) {
            into("org/sourcegrade/jagr/")
        }
    }
}
