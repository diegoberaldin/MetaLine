pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }

    plugins {
        kotlin("multiplatform").version(extra["kotlin.version"] as String)
        id("org.jetbrains.compose").version(extra["compose.version"] as String)
    }
}

rootProject.name = "MetaLine"

enableFeaturePreview("VERSION_CATALOGS")
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(
    ":core-common",
    ":core-localization",
    ":core-data",
    ":core-persistence",
    ":core-repository",

    ":feature-main",
    ":feature-main:intro",
    ":feature-project",
    ":feature-project:create",
    ":feature-project:statistics",
    ":feature-align",
)
