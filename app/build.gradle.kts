import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.googleKsp)
}

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(FileInputStream(localPropertiesFile))
}

android {
    namespace = "com.example.sonus"
    compileSdk = 37

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        applicationId = "com.example.sonus"
        minSdk = 24
        targetSdk = 37
        versionCode = 5
        versionName = "2.2.1"

        buildConfigField("String", "WG_SERVER_PUBLIC_KEY", "\"${localProperties.getProperty("WG_SERVER_PUBLIC_KEY") ?: ""}\"")
        buildConfigField("String", "WG_SERVER_ENDPOINT", "\"${localProperties.getProperty("WG_SERVER_ENDPOINT") ?: ""}\"")
        buildConfigField("String", "WG_CLIENT_PRIVATE_KEY", "\"${localProperties.getProperty("WG_CLIENT_PRIVATE_KEY") ?: ""}\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    implementation(project(":shared"))
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.material)

    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation("androidx.media:media:1.7.0")

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // WorkManager
    implementation(libs.androidx.work.runtime.ktx)

    // Paging 3
    implementation(libs.androidx.paging.runtime)
    implementation(libs.androidx.room.paging)

    // WireGuard
    implementation(libs.wireguard.tunnel)

    testImplementation(libs.junit)
}
