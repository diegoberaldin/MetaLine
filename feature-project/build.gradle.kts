plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
}

group = "feature.project"
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
                implementation(libs.essenty.instancekeeper)

                implementation(projects.coreCommon)
                implementation(projects.coreLocalization)
                implementation(projects.coreData)
                implementation(projects.coreRepository)

                implementation(projects.featureProject.create)
                implementation(projects.featureAlign)
            }
        }
    }
}
