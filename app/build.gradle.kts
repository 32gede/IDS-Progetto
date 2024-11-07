plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.kapt")
}

android {
    namespace = "com.example.progetto"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.progetto"
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
        viewBinding = true
    }
}

dependencies {
    // Firebase e Google Play Services
    implementation(platform(libs.firebase.bom))
    implementation(libs.google.firebase.firestore)
    implementation(libs.google.firebase.auth)
    implementation(libs.gms.play.services.auth)
    implementation (libs.firebase.storage)

    // AndroidX e Material Design
    implementation (libs.material.v160) // versione da aggiornare in base al progetto

    implementation(libs.appcompat)
    implementation(libs.constraintlayout)
    implementation(libs.material)


    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)

    // Glide
    implementation("com.github.bumptech.glide:glide:4.16.0")
    kapt("com.github.bumptech.glide:compiler:4.16.0")
    implementation ("com.github.bumptech.glide:glide:4.15.1")  // Aggiungi questa riga
    annotationProcessor ("com.github.bumptech.glide:compiler:4.15.1")  // Aggiungi anche questa per il supporto dell'annotation processor
    // Test
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}