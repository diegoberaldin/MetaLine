plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
}

group = "feature.project.metadata"
version = libs.versions.appVersion.get()

repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

kotlin {
    jvm {
        jvmToolchain(11)
        withJava()
    }
    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(compose.materialIconsExtended)

                implementation(libs.koin)
                implementation(libs.decompose)
                implementation(libs.decompose.extensions)
                implementation(libs.redundent)

                implementation(projects.coreCommon)
                implementation(projects.coreData)
                implementation(projects.coreLocalization)
                implementation(projects.coreRepository)
            }
        }
    }
}
