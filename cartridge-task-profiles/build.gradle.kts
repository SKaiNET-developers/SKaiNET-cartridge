// The public, versioned task-profile contracts (spec: docs/modules/ROOT/pages/task-profiles.adoc).
// KMP on purpose: profiles must be consumable from any Kotlin target an adapter or runner uses.
// Only the jvm target is wired in Phase 1; Android and native targets (a Kotlin/Native CLI runner's
// linuxArm64, the app adapters' androidTarget) land with their consumers in Phases 3-4.
plugins {
    kotlin("multiplatform") version "2.4.10"
}

kotlin {
    explicitApi()

    jvm()

    sourceSets {
        commonMain.dependencies {
            api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.11.0")
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.11.0")
        }
    }
}
