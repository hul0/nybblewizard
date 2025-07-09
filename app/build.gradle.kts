plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.republicwing.bufferwing"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.republicwing.bufferwing"
        minSdk = 28 // Good choice for modern Android, reduces backward compatibility issues
        targetSdk = 36
        versionCode = 4
        versionName = "2.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true // Essential: Enables R8/ProGuard for code shrinking
            isShrinkResources = true // Essential: Removes unused resources

            // R8 full mode can offer better size reduction but requires more careful testing.
            // It's a good idea to enable this for production builds after thorough testing.
            // property("android.enableR8.fullMode", "true") // Uncomment this for aggressive optimization

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            // For faster debug builds, keep these false.
            // If you absolutely need smaller debug APKs (e.g., for very slow installs),
            // you can enable them, but expect longer build times.
            isMinifyEnabled = false
            isShrinkResources = false
            applicationIdSuffix = ".debug" // Good practice to differentiate debug builds
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
        // viewBinding = true // Correctly commented out if not using ViewBinding.
    }

    // This exclusion can sometimes help, but test carefully as it might affect some libraries.
    // excludes += "kotlin/coroutines/coroutines.kotlin_builtins"

    // ⭐⭐⭐ HIGHLY RECOMMENDED FOR APK SIZE REDUCTION ON GOOGLE PLAY ⭐⭐⭐
    // Enable Android App Bundles for publishing. This is the most effective way
    // to reduce download size for users by delivering only the resources
    // (ABI, density, language) specific to their device.
    bundle {
        storeArchive {
            // For generating a universal APK for local testing, if needed.
            // enable = false
        }
        density {
            enableSplit = true
        }
        abi {
            enableSplit = true
        }
        language {
            enableSplit = true
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)

    // ⭐⭐⭐ CRUCIAL FOR APK SIZE: REMOVE UNUSED LIBRARIES ⭐⭐⭐
    // You confirmed you are not using Ads. This library is VERY LARGE.
    // REMOVE: implementation(libs.play.services.ads)

    // If your UI is fully Jetpack Compose, you don't need ConstraintLayout for Views.
    // REMOVE: implementation(libs.androidx.constraintlayout)

    val composeBom = platform("androidx.compose:compose-bom:2025.05.00") // Keep using the latest stable
    implementation(composeBom)
    androidTestImplementation(composeBom)

    // Core Compose
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.runtime:runtime")
    implementation("com.exyte:animated-navigation-bar:1.0.0")
    implementation("androidx.documentfile:documentfile:1.1.0") // Or the latest stable version

    // ⭐⭐⭐ CRUCIAL FOR APK SIZE: REMOVE OLD MATERIAL DESIGN LIBRARY ⭐⭐⭐
    // This is the old View-based Material library. You are using Compose Material 3.
    // REMOVE: implementation("com.google.android.material:material:1.12.0")

    // Material Design 3 (Compose)
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-core")
    implementation("androidx.compose.material:material-icons-extended") // For essential icons

    // For essential icons

    // ⭐⭐⭐ CONSIDER REMOVING or being selective with material-icons-extended ⭐⭐⭐
    // This library is VERY LARGE. If you only use a few icons from it,
    // consider importing them as SVG assets directly into your project
    // (e.g., using Android Studio's Vector Asset Studio) or using a more targeted icon library.
    // If you need many icons, keep it. Otherwise, removing it can save significant space.
    // REMOVE (if few icons used): implementation("androidx.compose.material:material-icons-extended")



    // Activity & Animation - ⭐ UPDATE TO LATEST STABLE ⭐
    implementation("androidx.activity:activity-compose:1.9.0") // Latest stable as of current knowledge
    implementation("androidx.compose.animation:animation")
    implementation("com.caverock:androidsvg-aar:1.4")
    // ViewModels in Compose - ⭐ UPDATE TO LATEST STABLE ⭐
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.0") // Latest stable as of current knowledge

    // Navigation - ⭐ UPDATE TO LATEST STABLE ⭐
    implementation("androidx.navigation:navigation-compose:2.7.7") // Latest stable as of current knowledge

    // Coroutines - ⭐ UPDATE TO LATEST STABLE ⭐
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0") // Latest stable as of current knowledge
    implementation("io.coil-kt:coil-compose:2.6.0")
    // Debug tools - ⭐ CRITICAL: Ensure these are ONLY in debugImplementation ⭐
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    debugImplementation("androidx.compose.ui:ui-tooling-preview") // <-- Moved from 'implementation'
    // This reduces the size of your release APK because preview tools are not included.

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
}