package org.sourcegrade.jagr.gradle.task

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.gradle.api.DefaultTask
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.listProperty
import org.sourcegrade.jagr.gradle.GraderInfo
import org.sourcegrade.jagr.gradle.SourceSetInfo
import org.sourcegrade.jagr.gradle.toInfo

@Suppress("LeakingThis")
abstract class WriteGraderInfoTask : DefaultTask() {

    @get:Input
    abstract val graderName: Property<String>

    @get:Input
    abstract val assignmentId: Property<String>

    @get:Input
    internal val sourceSets: ListProperty<SourceSetInfo> = project.objects.listProperty<SourceSetInfo>()
        .convention(project.extensions.getByType<SourceSetContainer>().map { it.toInfo() })

    @get:OutputFile
    val graderInfoFile = project.buildDir.resolve("resources/jagr/grader-info.json")

    init {
        dependsOn("compileJava")
        group = "jagr"
    }

    @TaskAction
    fun runTask() {
        val graderInfo = GraderInfo(
            graderName.get(),
            assignmentId.get(),
            mapOf(
                "grader" to listOf("graderPublic", "graderPrivate"),
                "solution" to listOf("main", "test"),
            ),
            sourceSets.get()
        )
        graderInfoFile.apply {
            parentFile.mkdirs()
            writeText(Json.encodeToString(graderInfo))
        }
    }
}
