import com.github.jengelman.gradle.plugins.shadow.transformers.Log4j2PluginsCacheFileTransformer
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    application
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.kapt) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.shadow)
    alias(libs.plugins.style)
}

dependencies {
    runtimeOnly(project("jagr-core"))
    implementation(project("jagr-launcher"))
    implementation(libs.clikt)
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

val projectVersion = file("version").readLines().first()
project.extra["apiVersion"] = projectVersion.replace("\\.[1-9]\\d*-SNAPSHOT|\\.0|\\.\\d*\$".toRegex(), "")

allprojects {
    apply(plugin = "org.sourcegrade.style")

    group = "org.sourcegrade"
    version = projectVersion

    project.findProperty("buildNumber")
        ?.takeIf { version.toString().contains("SNAPSHOT") }
        ?.also { version = version.toString().replace("SNAPSHOT", "RC$it") }

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
}
