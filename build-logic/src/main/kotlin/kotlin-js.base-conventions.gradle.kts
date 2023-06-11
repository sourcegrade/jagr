import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

extensions.getByName<KotlinMultiplatformExtension>("kotlin").apply {
    js(IR) {
        browser {
            runTask {
                devServer = devServer?.copy(open = false)
            }
        }
        binaries.executable()
        useCommonJs()
    }
}
