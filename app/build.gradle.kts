// Android Gradle Plugin 9.x trae Kotlin integrado:
// ya no se aplica el plugin "org.jetbrains.kotlin.android" ni el bloque kotlinOptions.
plugins {
    id("com.android.application")
}

android {
    namespace = "com.example.valesska_sanchez_lab15"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.valesska_sanchez_lab15"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    // Equivalente moderno de aaptOptions { noCompress "tflite" }:
    // el .tflite debe quedar sin comprimir para que ML Kit pueda mapearlo en memoria.
    androidResources {
        noCompress += "tflite"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // ML Kit Custom Image Labeling (modelo propio desde assets/)
    implementation("com.google.mlkit:image-labeling-custom:17.0.3")
}
