import java.text.SimpleDateFormat
import java.util.Date
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "id.xms.xtrakernelmanager"
    compileSdk = 37

    defaultConfig {
        applicationId = "id.xms.xtrakernelmanager"
        minSdk = 31
        targetSdk = 37
        versionCode = 3
        versionName = "3.2-Beta1"

        // Build Parsing
        val buildDate = SimpleDateFormat("yyyy.MM.dd").format(Date())
        buildConfigField("String", "BUILD_DATE", "\"$buildDate\"")
        buildConfigField("long", "BUILD_TIMESTAMP", "${System.currentTimeMillis()}L")

        vectorDrawables {
            useSupportLibrary = true
        }
    }

    androidResources {
        localeFilters += listOf("en", "id", "in")
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }

    signingConfigs {
        create("release") {
            val keystorePath = System.getenv("KEYSTORE_PATH")
                ?: project.findProperty("myKeystorePath")?.toString()
            val keystorePassword = System.getenv("KEYSTORE_PASSWORD")
                ?: project.findProperty("myKeystorePassword")?.toString()
            val keyAlias = System.getenv("KEY_ALIAS")
                ?: project.findProperty("myKeyAlias")?.toString()
            val keyPassword = System.getenv("KEY_PASSWORD")
                ?: project.findProperty("myKeyPassword")?.toString()

            if (!keystorePath.isNullOrEmpty() && file(keystorePath).exists()) {
                storeFile = file(keystorePath)
                storePassword = keystorePassword
                this.keyAlias = keyAlias
                this.keyPassword = keyPassword
            }
        }
    }

    buildTypes {
        debug {
            val gitHash = providers.exec {
                commandLine("git", "rev-parse", "--short", "HEAD")
            }.standardOutput.asText.get().trim()

            applicationIdSuffix = ".dev"
            versionNameSuffix = "-dev-$gitHash"
            // Add debug metadata to help Play Protect identify as development build
            buildConfigField("boolean", "IS_DEBUG_BUILD", "true")
            buildConfigField("boolean", "ENABLE_ACCESSIBILITY_SERVICE", "true")
            buildConfigField("boolean", "ENABLE_ROOT_FEATURES", "true")
            manifestPlaceholders["appLabel"] = "@string/app_name_short"
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release").takeIf { it.storeFile?.exists() == true }
                ?: signingConfigs.getByName("debug")
            ndk {
                debugSymbolLevel = "NONE"
            }
            // Play Protect compatibility optimizations
            buildConfigField("boolean", "IS_DEBUG_BUILD", "false")
            buildConfigField("boolean", "ENABLE_ACCESSIBILITY_SERVICE", "true")
            buildConfigField("boolean", "ENABLE_ROOT_FEATURES", "false")
            buildConfigField("String", "BUILD_VARIANT", "\"release\"")
            buildConfigField("String", "DISTRIBUTION_CHANNEL", "\"sideload\"")
            manifestPlaceholders["appLabel"] = "@string/app_name_short"

            // Additional optimizations for Play Protect
            isDebuggable = false
            isJniDebuggable = false
            isPseudoLocalesEnabled = false
            isCrunchPngs = true

            // Aggressive size optimizations
            packaging {
                jniLibs {
                    // Exclude unused ABIs in release
                    excludes += listOf("**/x86/**", "**/x86_64/**", "**/armeabi-v7a/**", "**/armeabi/**")
                }
            }
        }
    }
    splits {
        abi {
            isEnable = true
            reset()
            include("arm64-v8a")
            isUniversalApk = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
        lint.disable.add("NullSafeMutableLiveData")
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/*.kotlin_module"
            excludes += "/META-INF/DEPENDENCIES"
            excludes += "/META-INF/LICENSE*"
            excludes += "/META-INF/NOTICE*"
            excludes += "/kotlin/**"
            excludes += "/*.txt"
            excludes += "/*.properties"
            excludes += "/META-INF/versions/**"
            excludes += "/META-INF/proguard/**"
            excludes += "**/*.proto"
            excludes += "**/*.kotlin_builtins"
            excludes += "DebugProbesKt.bin"
            excludes += "/okhttp3/**"
            excludes += "/org/bouncycastle/**"
            excludes += "/org/conscrypt/**"
        }
        jniLibs {
            useLegacyPackaging = false
            // Keep only arm64-v8a for release
            pickFirsts += "**/*.so"
        }
    }
}

kotlin {
    jvmToolchain(21)
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
    }
}

dependencies {
    // Core Android
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.material)
    implementation(libs.androidx.work.runtime.ktx)

    // Compose BOM
    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.graphics.shapes)

    // Material 3 Expressive
    implementation(libs.androidx.compose.material.icons.extended)
    // Simple Icons (Brands)
    implementation(libs.compose.icons.simple)

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // ViewModel
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)

    // DataStore
    implementation(libs.androidx.datastore.preferences)

    // LibSu - Root Access
    implementation(libs.libsu.core)
    implementation(libs.libsu.service)
    implementation(libs.libsu.nio)

    // Google Play Services (for Play Protect compatibility)
    implementation(libs.play.services.base)
    implementation(libs.play.services.tasks)

    // TOML Parser
    implementation(libs.tomlj)

    // Accompanist (for system UI controller and pager)
    implementation(libs.accompanist.systemuicontroller)
    implementation(libs.accompanist.pager)
    implementation(libs.accompanist.pager.indicators)

    // Coil for image loading
    implementation(libs.coil.compose) {
        exclude(group = "androidx.appcompat")
        exclude(group = "androidx.lifecycle")
    }
    implementation(libs.coil.gif)

    implementation(libs.vico.compose.m3)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // Serialization
    implementation(libs.kotlinx.serialization.json)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(composeBom)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)

    // Debug
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
