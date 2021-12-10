import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.tasks.FatFrameworkTask

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("kotlinx-serialization")
    id("com.android.library")
    id("com.squareup.sqldelight") version "1.5.2"
}

repositories {
    gradlePluginPortal()
    google()
    mavenCentral()
    maven {
        url = uri("https://dl.bintray.com/kotlin/kotlin-eap")
    }
}

val iosFrameworkBaseName = "RiistaCommon"
// library versions recommended in kotlin 1.5.30 release details:
// - https://kotlinlang.org/docs/releases.html#release-details
val ktorVersion = "1.6.2"
val kotlinxSerializationVersion = "1.2.2"
val kotlinxDatetimeVersion = "0.2.1"
val statelyVersion = "1.1.10"
val statelyIsolateVersion = "${statelyVersion}-a1"
val sqlDelightVersion = "1.5.2"

kotlin {
    targets {
        val configureIOS: KotlinNativeTarget.() -> Unit = {
            binaries {
                framework {
                    baseName = iosFrameworkBaseName
                }
            }
        }
        android()

        val iosX64 = iosX64("iosX64", configureIOS)
        val iosArm32 = iosArm32("iosArm32", configureIOS)
        val iosArm64 = iosArm64("iosArm64", configureIOS)
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib-common"))

                // stately libraries allow using e.g. AtomicReference in common code
                // - at some point check whether kotlinx.atomicfu would be a better choice
                implementation("co.touchlab:stately-common:${statelyVersion}")
                implementation("co.touchlab:stately-concurrency:${statelyVersion}")
                implementation("co.touchlab:stately-isolate:${statelyIsolateVersion}")
                implementation("co.touchlab:stately-iso-collections:${statelyIsolateVersion}")

                // networking
                implementation("io.ktor:ktor-client-core:${ktorVersion}")

                // serialization (e.g. json)
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:${kotlinxSerializationVersion}")

                // datetime handling
                // NOTE: requires core library desugaring on android if target API version is below 26
                // - https://github.com/Kotlin/kotlinx-datetime#using-in-your-projects
                // - https://developer.android.com/studio/write/java8-support#library-desugaring
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:${kotlinxDatetimeVersion}")

                implementation("com.squareup.sqldelight:runtime:$sqlDelightVersion")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }
        val androidMain by getting {
            dependencies {
                implementation("androidx.core:core-ktx:1.3.2")

                // networking
                implementation("org.conscrypt:conscrypt-android:2.5.1")
                implementation("io.ktor:ktor-client-okhttp:${ktorVersion}")

                implementation("com.squareup.sqldelight:android-driver:$sqlDelightVersion")
            }
        }
        val androidTest by getting {
            dependencies {
                implementation(kotlin("test-junit"))
                implementation("junit:junit:4.13.1")
                implementation("com.squareup.sqldelight:sqlite-driver:$sqlDelightVersion")
            }
        }


        val iosMain by creating {
            dependencies {
                // networking
                implementation("io.ktor:ktor-client-ios:${ktorVersion}")

                implementation("com.squareup.sqldelight:native-driver:$sqlDelightVersion")
            }
            dependsOn(commonMain)
        }
        val iosTest by creating {
            dependencies {
                implementation(kotlin("test"))
            }
            dependsOn(commonTest)
        }

        val iosX64Main by getting
        val iosArm32Main by getting
        val iosArm64Main by getting
        configure(listOf(iosArm32Main, iosArm64Main, iosX64Main)) {
            dependsOn(iosMain)
        }
        val iosX64Test by getting
        val iosArm32Test by getting
        val iosArm64Test by getting
        configure(listOf(iosArm32Test, iosArm64Test, iosX64Test)) {
            dependsOn(iosTest)
        }

        all {
            languageSettings.optIn("kotlin.RequiresOptIn")
        }
    }
}

android {
    compileSdk = 30
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        minSdk = 21
        targetSdk = 30
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
}

// Compilation fails on 32bit iOS devices due to clang failure:
//   "error in backend: Relocation out of range".
// Optimize the bitcode file size according to https://youtrack.jetbrains.com/issue/KT-37368
// so that clang no longer complains
kotlin.iosArm32().binaries.getFramework(org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType.DEBUG).apply {
    val properties = "clangDebugFlags.ios_arm32=-Os"
    freeCompilerArgs = freeCompilerArgs + listOf(
        "-Xoverride-konan-properties=$properties"
    )
}

sqldelight {
    database("RiistaDatabase") {
        packageName = "fi.riista.common.database"
        schemaOutputDirectory = File("src/commonMain/sqldelight/schema")
    }
}

//region Creating a fat framework for iOS
val outputFrameworkDir = projectDir.resolve("output-framework")
val fatFrameworkPath = outputFrameworkDir.resolve("${project.name}.framework")

tasks.create<Delete>("deleteFatFramework") { delete = setOf(fatFrameworkPath) }

val createFatFramework by tasks.registering(FatFrameworkTask::class) {
    dependsOn("deleteFatFramework")
    val mode = "Release"
    val frameworks = arrayOf("iosArm32", "iosArm64", "iosX64")
        .map { kotlin.targets.getByName<KotlinNativeTarget>(it).binaries.getFramework(mode) }
    from(frameworks)
    baseName = iosFrameworkBaseName
    destinationDir = outputFrameworkDir
    group = "fat framework"
    description = "builds the fat framwrok"
    dependsOn(frameworks.map { it.linkTask })
}

//endregion
