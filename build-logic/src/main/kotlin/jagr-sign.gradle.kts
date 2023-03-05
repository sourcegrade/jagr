plugins {
    signing
}

extensions.configure<PublishingExtension> {
    publications {
        val maven = getByName<MavenPublication>("maven")
        extensions.configure<SigningExtension> {
            sign(maven)
        }
    }
}
