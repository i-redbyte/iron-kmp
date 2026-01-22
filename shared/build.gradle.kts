plugins {
    alias(libs.plugins.kotlin.multiplatform)
}

kotlin {
    macosArm64()
    macosX64()

    sourceSets {
        val commonMain by getting

        val macosMain by creating {
            dependsOn(commonMain)
        }

        val macosArm64Main by getting { dependsOn(macosMain) }
        val macosX64Main by getting { dependsOn(macosMain) }
    }
}
