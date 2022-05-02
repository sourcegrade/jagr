pluginManagement {
    includeBuild("build-logic")
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        maven("https://repo.spongepowered.org/repository/maven-public/")
    }
}

rootProject.name = "Jagr"

sequenceOf(
    "core",
    "grader-api",
    "launcher",
).forEach {
    val project = ":jagr-$it"
    include(project)
    project(project).projectDir = file(it)
}
