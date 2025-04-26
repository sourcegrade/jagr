import jetbrains.buildServer.configs.kotlin.BuildFeatures
import jetbrains.buildServer.configs.kotlin.BuildType
import jetbrains.buildServer.configs.kotlin.DslContext
import jetbrains.buildServer.configs.kotlin.FailureAction
import jetbrains.buildServer.configs.kotlin.buildFeatures.PullRequests
import jetbrains.buildServer.configs.kotlin.buildFeatures.commitStatusPublisher
import jetbrains.buildServer.configs.kotlin.buildFeatures.perfmon
import jetbrains.buildServer.configs.kotlin.buildFeatures.pullRequests
import jetbrains.buildServer.configs.kotlin.buildSteps.gradle
import jetbrains.buildServer.configs.kotlin.project
import jetbrains.buildServer.configs.kotlin.projectFeatures.githubIssues
import jetbrains.buildServer.configs.kotlin.triggers.vcs
import jetbrains.buildServer.configs.kotlin.version

/*
The settings script is an entry point for defining a TeamCity
project hierarchy. The script should contain a single call to the
project() function with a Project instance or an init function as
an argument.

VcsRoots, BuildTypes, Templates, and subprojects can be
registered inside the project using the vcsRoot(), buildType(),
template(), and subProject() methods respectively.

To debug settings scripts in command-line, run the

    mvnDebug org.jetbrains.teamcity:teamcity-configs-maven-plugin:generate

command and attach your debugger to the port 8000.

To debug in IntelliJ Idea, open the 'Maven Projects' tool window (View
-> Tool Windows -> Maven Projects), find the generate task node
(Plugins -> teamcity-configs -> teamcity-configs:generate), the
'Debug' option is available in the context menu for the task.
*/

project {
    val test = Test()
    val style = Style()
    val publish = Publish(test, style)

    buildType(test)
    buildType(style)
    buildType(publish)

    features {
        githubIssues {
            id = "PROJECT_EXT_3"
            displayName = "sourcegrade/jagr"
            repositoryURL = "https://github.com/sourcegrade/jagr"
            authType = accessToken {
                accessToken = "credentialsJSON:b7b250d4-b347-4611-bad6-a9dd7e3f44ff"
            }
            param("tokenId", "")
        }
    }
}

fun BuildType.configureVcs() {
    vcs {
        root(DslContext.settingsRoot)
    }
}

fun BuildType.configureTriggers() {
    triggers {
        vcs {
            triggerRules = """
                +:.
                -:comment=^\\[ci skip\\].*
            """.trimIndent()
            branchFilter = """
                +:*
                -:gh-pages
                """.trimIndent()
        }
    }
}

fun BuildFeatures.configureBaseFeatures() {
    perfmon {}
    commitStatusPublisher {
        vcsRootExtId = "${DslContext.settingsRoot.id}"
        publisher = github {
            githubUrl = "https://api.github.com"
            authType = personalToken {
                token = "credentialsJSON:b7b250d4-b347-4611-bad6-a9dd7e3f44ff"
            }
        }
    }
}

// TODO: Doesn't trigger builds
fun BuildFeatures.configurePullRequests() {
    pullRequests {
        vcsRootExtId = "${DslContext.settingsRoot.id}"
        provider = github {
            authType = token {
                token = "credentialsJSON:b7b250d4-b347-4611-bad6-a9dd7e3f44ff"
            }
            filterAuthorRole = PullRequests.GitHubRoleFilter.MEMBER_OR_COLLABORATOR
        }
    }
}

class Test : BuildType() {
    init {
        name = "test"

        configureVcs()
        configureTriggers()
        features {
            configureBaseFeatures()
//            configurePullRequests()
        }

        steps {
            gradle {
                id = "gradle_runner"
                tasks = "test"
                gradleParams = "--refresh-dependencies"
            }
        }
    }
}

class Style : BuildType() {
    init {
        name = "style"

        configureVcs()
        configureTriggers()
        features {
            configureBaseFeatures()
//            configurePullRequests()
        }

        steps {
            gradle {
                id = "gradle_runner"
                tasks = "ktlintCheck"
            }
        }
    }
}

class Publish(test: BuildType, style: BuildType) : BuildType() {
    init {
        name = "publish"

        dependencies {
            snapshot(test) {
                onDependencyFailure = FailureAction.FAIL_TO_START
            }
            snapshot(style) {
                onDependencyFailure = FailureAction.FAIL_TO_START
            }
        }

        configureVcs()
        features {
            configureBaseFeatures()
        }

        requirements {
            contains("env.AGENT_NAME", "publishing")
        }

        steps {
            gradle {
                id = "gradle_runner"
                tasks = "publish"
                gradleParams = "--refresh-dependencies"
            }
        }
    }
}
