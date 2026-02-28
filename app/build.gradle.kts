plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.auto"
    compileSdk = 33  // Android 13

    defaultConfig {
        applicationId = "com.example.auto"
        minSdk = 21
        targetSdk = 33  // Android 13
        versionCode = 1
        versionName = "1.0.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
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
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.3"
    }
}
}

dependencies {
    // Jetpack Compose
    implementation("androidx.compose.ui:ui:1.4.3")
    implementation("androidx.compose.ui:ui-tooling-preview:1.4.3")
    implementation("androidx.compose.material:material:1.4.3")
    implementation("androidx.compose.foundation:foundation:1.4.3")
    
    // AndroidX Core
    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.activity:activity-compose:1.6.1")
    
    // Lifecycle & ViewModel
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.5.1")
    
    // WorkManager for background tasks
    implementation("androidx.work:work-runtime-ktx:2.8.1")
    
    // AppCompat for theme
    implementation("androidx.appcompat:appcompat:1.6.1")
    
    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    
    debugImplementation("androidx.compose.ui:ui-tooling:1.4.3")
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.4.3")
}
