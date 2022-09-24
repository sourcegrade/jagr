package org.sourcegrade.jagr.gradle

import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.container
import javax.inject.Inject

abstract class JagrExtension @Inject constructor(
    project: Project,
    objectFactory: ObjectFactory,
) : ExtensionAware {
    abstract val assignmentId: Property<String>
    val graders: NamedDomainObjectContainer<GraderSourceSetConfiguration> = project.container()
    val submissions: NamedDomainObjectContainer<SubmissionSourceSetConfiguration> = project.container()
    fun graders(configure: NamedDomainObjectContainer<GraderSourceSetConfiguration>.() -> Unit) = graders.configure()
    fun submissions(configure: NamedDomainObjectContainer<SubmissionSourceSetConfiguration>.() -> Unit) = submissions.configure()
}
