import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
}

group = "it.meta.line"
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
                implementation(libs.essenty.instancekeeper)

                implementation(compose.foundation)
                implementation(compose.animation)

                implementation(libs.koin)

                implementation(projects.coreCommon)
                implementation(projects.coreData)
                implementation(projects.coreLocalization)
                implementation(projects.corePersistence)
                implementation(projects.coreRepository)

                implementation(projects.featureMain)
                implementation(projects.featureProject)
                implementation(projects.featureProject.create)
                implementation(projects.featureProject.statistics)
                implementation(projects.featureProject.settings)
                implementation(projects.featureAlign)
            }
        }
        val jvmTest by getting
    }
}

compose.desktop {
    application {
        mainClass = "MetaLineKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "MetaLine"
            packageVersion = libs.versions.appVersion.get().substring(0, 5)
            version = libs.versions.buildNumber.get()
            includeAllModules = true
            macOS {
                iconFile.set(project.file("res/icon.icns"))
                setDockNameSameAsPackageName = true
            }
            windows {
                iconFile.set(project.file("res/icon.ico"))
            }
            linux {
                iconFile.set(project.file("res/icon.png"))
            }
        }
    }
}
