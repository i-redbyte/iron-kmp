plugins {
    alias(libs.plugins.kotlin.multiplatform)
}

kotlin {
    macosArm64()
    macosX64()
    jvm()
    sourceSets {
        val commonMain by getting  {
            dependencies {
                implementation(libs.coroutines)
            }
        }

        val macosMain by creating {
            dependsOn(commonMain)
        }

        val macosArm64Main by getting { dependsOn(macosMain) }
        val macosX64Main by getting { dependsOn(macosMain) }
    }
}
