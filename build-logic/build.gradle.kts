plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(libs.build.indra)
    implementation(libs.build.indra.sonatype)
    compileOnly(files(libs::class.java.protectionDomain.codeSource.location))
}
