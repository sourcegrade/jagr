dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        maven("https://repo.spongepowered.org/repository/maven-public/")
    }
}

rootProject.name = "jagr"

fun List<String>.includeProjects(vararg projects: String) {
    val pathName = joinToString("") { "$it-" }
    val pathDir = joinToString("") { "$it/" }
    projects.forEach { projectName ->
        val project = ":jagr-$pathName$projectName"
        include(project)
        project(project).projectDir = file(pathDir + projectName)
    }
}
emptyList<String>().includeProjects(
    "agent",
    "dispatcher",

    "launcher",

    "core",
    "domain",

    "compiler",
    "grader",
)

listOf("agent").includeProjects("backend", "ui", "core")

listOf("compiler").includeProjects("api", "core")
listOf("compiler", "api").includeProjects("jvm", "java", "kotlin", "racket")
listOf("compiler", "core").includeProjects("jvm", "java", "kotlin", "racket")

listOf("dispatcher").includeProjects("backend", "ui", "core")

listOf("domain").includeProjects("io", "scheduler")

listOf("grader").includeProjects("api", "core")
listOf("grader", "api").includeProjects("jvm", "racket")
listOf("grader", "core").includeProjects("jvm", "racket")

listOf("launcher").includeProjects("cli", "gradle-plugin")
