import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.tasks.FatFrameworkTask

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("kotlinx-serialization")
    id("com.android.library")
    id("com.squareup.sqldelight") version "1.5.5"
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
val ktorVersion = "2.3.0"
val kotlinxSerializationVersion = "1.5.0"
val kotlinxDatetimeVersion = "0.4.0"
val statelyVersion = "1.2.5"
val sqlDelightVersion = "1.5.5"

kotlin {
    val configureIOS: KotlinNativeTarget.() -> Unit = {
        binaries {
            framework {
                baseName = iosFrameworkBaseName
                // static framework required by the CrashKiOS as app thinning on iOS messes up
                // the linking done under the hood:
                // https://github.com/rickclephas/NSExceptionKt/issues/12#issuecomment-1534413364
                // https://youtrack.jetbrains.com/issue/KT-58461
                // https://github.com/firebase/firebase-ios-sdk/blob/master/docs/firebase_in_libraries.md
                isStatic = true
            }
        }
    }

    android()
    iosX64("iosX64", configureIOS)
    iosArm64("iosArm64", configureIOS)

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib-common"))

                // stately libraries allow using e.g. AtomicReference in common code
                // - at some point check whether kotlinx.atomicfu would be a better choice
                implementation("co.touchlab:stately-concurrency:${statelyVersion}")

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

                // Base64 encoding
                val encoding = "1.0.3" // Use 1.1.0 for kotlin 1.6
                implementation("io.matthewnelson.kotlin-components:encoding-base64:$encoding")

                // requires static linkage! See configureIos { binaries.framework.isStatic }
                implementation("co.touchlab.crashkios:crashlytics:0.8.2")
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
                implementation("androidx.core:core-ktx:1.10.1")

                // networking
                implementation("org.conscrypt:conscrypt-android:2.5.1")
                implementation("io.ktor:ktor-client-okhttp:${ktorVersion}")

                implementation("com.squareup.sqldelight:android-driver:$sqlDelightVersion")
            }
        }
        val androidUnitTest by getting {
            dependencies {
                implementation(kotlin("test-junit"))
                implementation("junit:junit:4.13.2")
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

        val iosX64Main by getting
        val iosArm64Main by getting
        configure(listOf(iosArm64Main, iosX64Main)) {
            dependsOn(iosMain)
        }

        all {
            languageSettings.optIn("kotlin.RequiresOptIn")
        }
    }
}

android {
    compileSdk = 31
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        minSdk = 21
        targetSdk = 31
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    namespace = "fi.riista.common.android"
}

sqldelight {
    database("RiistaDatabase") {
        packageName = "fi.riista.common.database"
        schemaOutputDirectory = File("src/commonMain/sqldelight/schema")
    }
}


// region Fix building on iOS

// See: https://youtrack.jetbrains.com/issue/KT-51359
afterEvaluate {
    if (System.getProperty("os.name") == "Mac OS X") {
        val kotlinNativeHome =
            org.jetbrains.kotlin.gradle.utils.NativeCompilerDownloader(project).compilerDirectory.absolutePath

        exec {
            commandLine(
                "install_name_tool", "-change", "@rpath/libc++.1.dylib", "/usr/lib/libc++.1.dylib",
                "$kotlinNativeHome/konan/nativelib/libllvmstubs.dylib"
            )
        }
    }
}

// endregion
