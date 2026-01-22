plugins {
    alias(libs.plugins.kotlin.multiplatform)
}

kotlin {
    macosArm64()
    macosX64()

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":shared"))
            }
        }
    }

    targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget>().configureEach {
        binaries {
            executable {
                entryPoint = "main"
            }
        }
    }
}
