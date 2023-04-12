plugins {
    signing
}

extensions.configure<PublishingExtension> {
    publications.withType<MavenPublication> {
        val publication = this
        extensions.configure<SigningExtension> {
            sign(publication)
        }
    }
}
