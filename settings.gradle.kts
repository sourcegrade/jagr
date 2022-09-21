dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenLocal() // TODO: Remove
        mavenCentral()
        maven("https://repo.spongepowered.org/repository/maven-public/")
    }
}

rootProject.name = "jagr"

include(":jagr-gradle")
project(":jagr-gradle").projectDir = file("launcher/gradle-plugin")

sequenceOf(
    "core",
    "grader-api",
    "launcher",
).forEach {
    val project = ":jagr-$it"
    include(project)
    project(project).projectDir = file(it)
}
