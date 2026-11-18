plugins {
    id("hisaab.jvm.library")
}

// Deliberately no Android, no Compose, no network dependency. See docs/ARCHITECTURE.md.
dependencies {
    testImplementation(libs.junit)
}
