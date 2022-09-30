package org.sourcegrade.jagr.gradle.task.grader

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.listProperty
import org.gradle.kotlin.dsl.mapProperty
import org.gradle.kotlin.dsl.property
import org.sourcegrade.jagr.gradle.extension.GraderConfiguration
import org.sourcegrade.jagr.gradle.extension.JagrExtension
import org.sourcegrade.jagr.gradle.forEachFile
import org.sourcegrade.jagr.gradle.getFiles
import org.sourcegrade.jagr.gradle.task.JagrTaskFactory
import org.sourcegrade.jagr.launcher.env.Jagr
import org.sourcegrade.jagr.launcher.io.GraderInfo
import org.sourcegrade.jagr.launcher.io.SourceSetInfo
import java.io.File

@Suppress("LeakingThis")
abstract class GraderWriteInfoTask : DefaultTask(), GraderTask {

    private val primaryContainer = project.extensions.getByType<JagrExtension>().graders
    private val submissionContainer = project.extensions.getByType<JagrExtension>().submissions

    @get:Input
    @get:Optional
    abstract val rubricProviderName: Property<String>

    @get:Input
    val graderFiles: ListProperty<String> = project.objects.listProperty<String>().value(
        configurationName.map { c -> primaryContainer[c].getFilesRecursive() }
    )

    @get:Input
    val solutionFiles: ListProperty<String> = project.objects.listProperty<String>().value(
        solutionConfigurationName.map { c -> submissionContainer[c].sourceSets.flatMap { it.getFiles() } }
    )

    @get:Input
    val dependencies: MapProperty<String, List<String>> = project.objects.mapProperty<String, List<String>>().value(
        configurationName.map { c -> primaryContainer[c].getAllDependenciesRecursive() }
    )

    @get:OutputFile
    val graderInfoFile: Property<File> = project.objects.property<File>()
        .value(configurationName.map { project.buildDir.resolve("resources/jagr/$it/grader-info.json") })

    init {
        group = "jagr resources"
        dependsOn("compileJava")
    }

    private fun GraderConfiguration.getFilesRecursive(): List<String> {
        val result = mutableListOf<String>()
        sourceSets.forEach { sourceSet ->
            sourceSet.forEachFile { result.add(it) }
        }
        // technically this is a race condition, but we can't use Provider.zip because the value is not always configured
        if (parentConfiguration.isPresent) {
            result.addAll(parentConfiguration.get().getFilesRecursive())
        }
        return result
    }

    private fun GraderConfiguration.getAllDependenciesRecursive(): Map<String, List<String>> {
        val ownDependencies = getAllDependencies("grader") +
            solutionConfiguration.get().getAllDependencies()
        return if (parentConfiguration.isPresent) {
            ownDependencies + parentConfiguration.get().getAllDependenciesRecursive()
        } else {
            ownDependencies
        }
    }

    private fun GraderConfiguration.getRubricProviderNameRecursive(): String {
        return if (rubricProviderName.isPresent) {
            rubricProviderName.get()
        } else if (parentConfiguration.isPresent) {
            parentConfiguration.get().getRubricProviderNameRecursive()
        } else {
            throw GradleException(
                "No rubricProviderName defined for grader configuration ${configurationName.get()} or its parents"
            )
        }
    }

    @TaskAction
    fun runTask() {
        val configuration: GraderConfiguration = primaryContainer[configurationName.get()]
        val graderInfo = GraderInfo(
            assignmentId.get(),
            Jagr.version,
            listOf(
                SourceSetInfo("grader", graderFiles.get()),
                SourceSetInfo("solution", solutionFiles.get())
            ),
            dependencies.get(),
            emptyList(),
            graderName.get(),
            rubricProviderName.orNull ?: run { configuration.getRubricProviderNameRecursive() },
        )
        graderInfoFile.get().apply {
            parentFile.mkdirs()
            writeText(Json.encodeToString(graderInfo))
        }
    }

    internal object Factory : JagrTaskFactory<GraderWriteInfoTask, GraderConfiguration> {
        override fun determineTaskName(name: String) = "${name}WriteGraderInfo"
        override fun configureTask(task: GraderWriteInfoTask, project: Project, configuration: GraderConfiguration) {
            task.description = "Runs the ${task.sourceSetNames.get()} grader"
            task.rubricProviderName.set(configuration.rubricProviderName)
        }
    }
}
