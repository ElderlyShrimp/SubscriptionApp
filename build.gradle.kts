plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android") // Убираем указание версии
    id("org.jetbrains.kotlin.plugin.compose") // Убираем указание версии
}

android {
    namespace = "com.example.subscriptionsapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.subscriptionsapp"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true // Включаем поддержку Compose
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "2.0.1"
    }

}

dependencies {
    implementation(platform("com.google.firebase:firebase-bom:32.7.2"))
    // Compose dependencies
    implementation("androidx.compose.ui:ui:1.5.3")
    implementation("androidx.compose.material3:material3:1.1.2")
    implementation("androidx.compose.foundation:foundation:1.5.3")
    implementation("androidx.compose.ui:ui-tooling-preview:1.5.3")

    // For KeyboardOptions (compose.ui.text.input)
    implementation("androidx.compose.ui:ui-text:1.5.3")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.5.3")

    // Test dependencies
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")

    implementation("com.google.firebase:firebase-auth-ktx:22.3.1")
    implementation("com.google.firebase:firebase-auth:22.3.1")

    implementation("com.google.firebase:firebase-firestore-ktx:24.10.3")

    // Firestore dependency
    implementation("com.google.firebase:firebase-firestore-ktx:24.7.2")

    implementation("io.coil-kt:coil-compose:2.4.0")

    implementation ("androidx.navigation:navigation-compose:2.5.0")

    implementation ("androidx.work:work-runtime-ktx:2.9.0")
}

apply(plugin = "com.google.gms.google-services")

