package org.sourcegrade.jagr.gradle.task.grader

import kotlinx.coroutines.runBlocking
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.provider.Property
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.property
import org.sourcegrade.jagr.gradle.extension.GraderConfiguration
import org.sourcegrade.jagr.gradle.extension.JagrExtension
import org.sourcegrade.jagr.gradle.task.JagrTaskFactory
import org.sourcegrade.jagr.gradle.task.submission.SubmissionWriteInfoTask
import org.sourcegrade.jagr.launcher.env.Config
import org.sourcegrade.jagr.launcher.env.Environment
import org.sourcegrade.jagr.launcher.env.Jagr
import org.sourcegrade.jagr.launcher.env.SystemResourceJagrFactory
import org.sourcegrade.jagr.launcher.env.gradingQueueFactory
import org.sourcegrade.jagr.launcher.env.logger
import org.sourcegrade.jagr.launcher.executor.Executor
import org.sourcegrade.jagr.launcher.executor.SyncExecutor
import org.sourcegrade.jagr.launcher.executor.emptyCollector
import org.sourcegrade.jagr.launcher.io.GradedRubricExporter
import org.sourcegrade.jagr.launcher.io.GradingBatch
import org.sourcegrade.jagr.launcher.io.ResourceContainer
import org.sourcegrade.jagr.launcher.io.addResource
import org.sourcegrade.jagr.launcher.io.buildGradingBatch
import org.sourcegrade.jagr.launcher.io.buildResourceContainer
import org.sourcegrade.jagr.launcher.io.buildResourceContainerInfo
import org.sourcegrade.jagr.launcher.io.createResourceContainer
import org.sourcegrade.jagr.launcher.io.logGradedRubric
import org.sourcegrade.jagr.launcher.io.logHistogram
import org.sourcegrade.jagr.launcher.io.writeIn
import java.io.File

@Suppress("LeakingThis")
abstract class GraderRunTask : DefaultTask(), GraderTask {

    @get:InputFile
    val graderInfoFile: Property<File> = project.objects.property<File>()
        .value(configurationName.map { project.buildDir.resolve("resources/jagr/$it/grader-info.json") })

    @get:InputFile
    val submissionInfoFile: Property<File> = project.objects.property<File>()
        .value(submissionConfigurationName.map { project.buildDir.resolve("resources/jagr/$it/submission-info.json") })

    init {
        group = "verification"
        dependsOn(submissionConfigurationName.map(SubmissionWriteInfoTask.Factory::determineTaskName))
        dependsOn(configurationName.map(GraderWriteInfoTask.Factory::determineTaskName))
    }

    private fun GraderConfiguration.getConfigRecursive(): Config {
        return if (config.isPresent) {
            config.get()
        } else if (parentConfiguration.isPresent) {
            parentConfiguration.get().getConfigRecursive()
        } else {
            Config()
        }
    }

    @TaskAction
    fun runTask() {
        runBlocking {
            grade()
        }
    }

    private val exporterHTML = Jagr.injector.getInstance(GradedRubricExporter.HTML::class.java)
    private val exporterMoodle = Jagr.injector.getInstance(GradedRubricExporter.Moodle::class.java)

    private suspend fun grade() {
        val jagrExtension = project.extensions.getByType<JagrExtension>()
        val configuration = jagrExtension.graders[configurationName.get()]
        val jagr = SystemResourceJagrFactory.create(GradleLaunchConfiguration(configuration.getConfigRecursive()))
        jagr.logger.info("Starting Jagr v${Jagr.version}")
        val batch: GradingBatch = buildGradingBatch {
            addGrader(
                buildResourceContainer {
                    info = buildResourceContainerInfo {
                        name = "grader"
                    }
                    project.extensions
                        .getByType<SourceSetContainer>()
                        .filter { configuration.matchRecursive(it) }
                        .forEach { sourceSet -> sourceSet.allSource.sourceDirectories.writeToContainer(this) }
                    addResource {
                        name = "grader-info.json"
                        graderInfoFile.get().inputStream().use { input ->
                            outputStream.use { output ->
                                input.transferTo(output)
                            }
                        }
                    }
                }
            )
            addSubmission(
                buildResourceContainer {
                    info = buildResourceContainerInfo {
                        name = "submission"
                    }
                    for (sourceSet in jagrExtension.submissions[submissionConfigurationName.get()].sourceSets) {
                        writeSourceSet(sourceSet)
                    }
                    addResource {
                        name = "submission-info.json"
                        submissionInfoFile.get().inputStream().use { input ->
                            outputStream.use { output ->
                                input.transferTo(output)
                            }
                        }
                    }
                }
            )
            val sourceSetContainer = project.extensions.getByType<SourceSetContainer>()
            val allSourceSets: Set<String> = configuration.getSourceSetNamesRecursive() +
                jagrExtension.submissions[solutionConfigurationName.get()].sourceSetNames.get()
            sourceSetContainer.asSequence()
                .filter { it.name in allSourceSets }
                .flatMap {
                    sequenceOf(
                        project.configurations[it.runtimeClasspathConfigurationName],
                        project.configurations[it.compileClasspathConfigurationName],
                    )
                }
                .flatMap { it.resolvedConfiguration.resolvedArtifacts }
                .filter {
                    !(
                        it.id.componentIdentifier.displayName.startsWith("org.sourcegrade") &&
                            it.id.componentIdentifier.displayName.contains("jagr")
                        )
                }
                .map { it.file }
                .forEach {
                    addLibrary(createResourceContainer(it))
                }
        }
        val queue = jagr.gradingQueueFactory.create(batch)
        jagr.logger.info("Executor mode 'gradle' :: expected submission: ${batch.expectedSubmissions}")
        val executor: Executor = SyncExecutor(jagr)
        val collector = emptyCollector(jagr)
        // TODO: Properly configure task output
        val rubricOutputDir = project.buildDir.resolve("resources/jagr/${configurationName.get()}/rubrics/")
        collector.setListener { result ->
            result.rubrics.keys.forEach {
                it.logGradedRubric(jagr)
                val resource = exporterHTML.export(it)
                resource.writeIn(rubricOutputDir)
                val moodleResource = exporterMoodle.export(it)
                moodleResource.writeIn(rubricOutputDir)
            }
        }
        collector.allocate(queue)
        executor.schedule(queue)
        executor.start(collector)
        Environment.cleanupMainProcess()
        val rubricCount = collector.withGradingFinished { gradingFinished ->
            gradingFinished.logHistogram(jagr)
            gradingFinished.sumOf { it.rubrics.size }
        }
        if (rubricCount == 0) {
            jagr.logger.warn("No rubrics!")
        } else {
            jagr.logger.info("Exported $rubricCount rubrics")
        }
        jagr.logger.info("See rubric at file://${rubricOutputDir.absolutePath}/result.html")
    }

    /**
     * Returns true if [sourceSet] is in this configuration or any parents
     */
    private fun GraderConfiguration.matchRecursive(sourceSet: SourceSet): Boolean {
        val jagr = project.extensions.getByType<JagrExtension>()
        val solutionSourceSetNames = jagr.submissions[solutionConfigurationName.get()].sourceSetNames.get()
        return sourceSet.name in sourceSetNames.get() || sourceSet.name in solutionSourceSetNames ||
            (parentConfiguration.isPresent && parentConfiguration.get().matchRecursive(sourceSet))
    }

    private fun ResourceContainer.Builder.writeSourceSet(sourceSet: SourceSet) {
        sourceSet
            .allSource
            .sourceDirectories
            .writeToContainer(this)
    }

    private fun FileCollection.writeToContainer(builder: ResourceContainer.Builder) {
        forEach { sourceDirectory ->
            sourceDirectory.walk().filter { it.isFile }.forEach { file ->
                builder.addResource {
                    name = file.relativeTo(sourceDirectory).path
                    file.inputStream().use { input ->
                        outputStream.use { output ->
                            input.transferTo(output)
                        }
                    }
                }
            }
        }
    }

    internal object Factory : JagrTaskFactory<GraderRunTask, GraderConfiguration> {
        override fun determineTaskName(name: String) = "${name}Run"
        override fun configureTask(task: GraderRunTask, project: Project, configuration: GraderConfiguration) {
            task.description = "Runs the ${task.sourceSetNames.get()} grader"
        }
    }
}
