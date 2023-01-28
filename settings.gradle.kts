dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven("https://repo.spongepowered.org/repository/maven-public/")
    }
}

rootProject.name = "jagr"

include(":jagr-launcher-gradle-plugin")
project(":jagr-launcher-gradle-plugin").projectDir = file("launcher/gradle-plugin")

sequenceOf(
    "core",
    "grader-api",
    "launcher",
).forEach {
    val project = ":jagr-$it"
    include(project)
    project(project).projectDir = file(it)
}
