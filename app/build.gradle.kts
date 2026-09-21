plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.delprod.kanban"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.delprod.kanban"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
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

        // Apache POI використовує API, які потребують desugaring на старих API levels
        isCoreLibraryDesugaringEnabled = true

        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.recyclerview)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
    implementation ("androidx.lifecycle:lifecycle-viewmodel-ktx:2.9.4")
    implementation ("androidx.fragment:fragment-ktx:1.8.9")
    //RETROFIT
    implementation ("com.squareup.retrofit2:retrofit:3.0.0")
    implementation ("com.squareup.retrofit2:converter-gson:3.0.0")
    implementation ("com.squareup.okhttp3:okhttp:5.2.1")
    implementation ("com.squareup.okhttp3:logging-interceptor:5.2.1")

    // Класичні Android View (без Compose)
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.0") // lifecycleScope

    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")

    // Парсинг .xls (legacy BIFF формат)
    implementation("org.apache.poi:poi:5.2.5")


    // Якщо знадобиться підтримка .xlsx теж:
    implementation("org.apache.poi:poi-ooxml:5.2.5")

    // Encrypted storage for access/refresh tokens (see data/auth/TokenStore.kt) — the business
    // database itself no longer lives on-device, it's served by /backend over REST.
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    // Google Sign-In / автентифікація (обрати актуальний варіант під проєкт)
    implementation("androidx.credentials:credentials:1.3.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.3.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")

    //barcode generator
    implementation("com.google.zxing:core:3.5.3")

}