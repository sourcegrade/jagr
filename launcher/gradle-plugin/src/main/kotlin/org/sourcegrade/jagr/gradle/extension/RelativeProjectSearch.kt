package org.sourcegrade.jagr.gradle.extension

import org.gradle.api.Project

fun Project.relative(path: String): Project {
    if (path.isEmpty()) return this
    return project.project(path)
}
