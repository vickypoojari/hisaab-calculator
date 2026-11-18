plugins {
    `kotlin-dsl`
}

group = "in.simply.hisaab.buildlogic"

// The convention plugins run inside Gradle and compile against AGP 9.x, which needs Java 17.
// This does not affect the Java level of the app and library modules (still 11).
java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.compose.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("hisaabJvmLibrary") {
            id = "hisaab.jvm.library"
            implementationClass = "HisaabJvmLibraryConventionPlugin"
        }
        register("hisaabAndroidLibrary") {
            id = "hisaab.android.library"
            implementationClass = "HisaabAndroidLibraryConventionPlugin"
        }
    }
}
