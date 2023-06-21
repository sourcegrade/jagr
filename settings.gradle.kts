dependencyResolutionManagement {
//    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenLocal()
        gradlePluginPortal()
        mavenCentral()
        maven("https://repo.spongepowered.org/repository/maven-public/")
    }
}

pluginManagement {
    includeBuild("build-logic")
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

rootProject.name = "jagr"

include(":jagr-launcher-gradle-plugin")
project(":jagr-launcher-gradle-plugin").projectDir = file("launcher/gradle-plugin")

sequenceOf(
    "api",
    "api-grader",
    "api-platform",
    "api-web",
    "app-classroom",
    "app-classroom-backend",
    "app-classroom-ui",
    "core",
    "core-db",
    "domain",
    "launcher",
).forEach {
    val project = ":jagr-$it"
    include(project)
    project(project).projectDir = file(it.replace('-', '/'))
}
