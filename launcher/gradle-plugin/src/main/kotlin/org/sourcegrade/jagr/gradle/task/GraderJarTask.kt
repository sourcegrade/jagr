package org.sourcegrade.jagr.gradle.task

import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.getByType

@Suppress("LeakingThis")
abstract class GraderJarTask : Jar() {

    @get:Input
    abstract val graderName: Property<String>

    @get:Input
    abstract val graderSourceSets: ListProperty<String>

    @get:InputFile
    val graderInfoFile = project.buildDir.resolve("resources/jagr/grader-info.json")

    init {
        dependsOn("writeGraderInfo")
        group = "build"
        archiveFileName.set(graderName.map { "$it-${project.version}.jar" })
        from(graderInfoFile)
        val sourceSets = project.extensions.getByType<SourceSetContainer>()
        from(sourceSets["main"].allSource)
        from(sourceSets["test"].allSource)
        graderSourceSets.get().forEach {
            from(sourceSets[it].allSource)
        }
    }
}
