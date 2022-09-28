package org.sourcegrade.jagr.gradle

import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.provider.Property

abstract class JagrExtension : ExtensionAware {
    abstract val assignmentId: Property<String>
    abstract val graders: NamedDomainObjectContainer<GraderConfiguration>
    abstract val submissions: NamedDomainObjectContainer<SubmissionConfiguration>
    fun graders(configure: NamedDomainObjectContainer<GraderConfiguration>.() -> Unit) = graders.configure()
    fun submissions(configure: NamedDomainObjectContainer<SubmissionConfiguration>.() -> Unit) = submissions.configure()
}
