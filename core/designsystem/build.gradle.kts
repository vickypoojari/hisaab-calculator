plugins {
    id("hisaab.android.library")
}

android {
    namespace = "in.simply.hisaab.core.designsystem"
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    testImplementation(libs.junit)
    testImplementation(kotlin("test-junit"))
}
