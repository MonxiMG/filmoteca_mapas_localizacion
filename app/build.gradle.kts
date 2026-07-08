plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")

    // Plugin necesario para conectar la app con Firebase.
    id("com.google.gms.google-services")
}

android {
    namespace = "es.ua.eps.filmoteca"
    compileSdk = 36

    defaultConfig {
        applicationId = "es.ua.eps.filmoteca"
        minSdk = 24
        targetSdk = 36
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

    // Activación de XML, ViewBinding y Compose.
    buildFeatures {
        viewBinding = true
        compose = true
        buildConfig = true
    }

    // Versión del compilador de Compose.
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }

    // Configuración de Java 17.
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    // Configuración de Kotlin 17.
    kotlinOptions {
        jvmTarget = "17"
    }

    // Exclusión de licencias duplicadas para evitar conflictos de empaquetado.
    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
}

dependencies {
    // --- Librerías base ---
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // --- Google Sign In ---
    implementation("com.google.android.gms:play-services-auth:21.4.0")

    // --- Firebase ---
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-messaging")

    // --- Activity para XML y Compose ---
    implementation("androidx.activity:activity-ktx:1.9.3")
    implementation("androidx.activity:activity-compose:1.9.3")

    // --- Compose con BOM ---
    val composeBom = platform("androidx.compose:compose-bom:2024.09.03")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material3:material3-window-size-class")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // --- Test ---
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}

// Seguridad adicional para evitar que Gradle use versiones incompatibles de Activity.
configurations.all {
    resolutionStrategy {
        force(
            "androidx.activity:activity:1.9.3",
            "androidx.activity:activity-ktx:1.9.3",
            "androidx.activity:activity-compose:1.9.3"
        )
    }
}