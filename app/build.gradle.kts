plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "icy.tapviz.app"
    compileSdk = 37

    defaultConfig {
        applicationId = "icy.tapviz.app"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "STORE_NAME", "\"Google Play\"")
        buildConfigField("String", "STORE_URL", "\"https://play.google.com/store/apps/details?id=icy.tapviz.app\"")
        buildConfigField("String", "STORE_REVIEW_URL", "\"market://details?id=icy.tapviz.app\"")
    }

    flavorDimensions += "store"
    productFlavors {
        create("googlePlay") {
            dimension = "store"
            applicationIdSuffix = ".gp"
            buildConfigField("String", "STORE_NAME", "\"Google Play\"")
            buildConfigField("String", "STORE_URL", "\"https://play.google.com/store/apps/details?id=icy.tapviz.app\"")
            buildConfigField("String", "STORE_REVIEW_URL", "\"market://details?id=icy.tapviz.app\"")
        }
        create("ruStore") {
            dimension = "store"
            applicationIdSuffix = ".rustore"
            buildConfigField("String", "STORE_NAME", "\"RuStore\"")
            buildConfigField("String", "STORE_URL", "\"https://apps.rustore.ru/app/icy.tapviz.app\"")
            buildConfigField("String", "STORE_REVIEW_URL", "\"rustore://details?id=icy.tapviz.app\"")
        }
    }

    buildTypes {
        debug {
            buildConfigField("String", "API_BASE_URL", "\"${providers.gradleProperty("tapvizDebugApiBaseUrl").getOrElse("https://tapviz.vorlan.ru")}\"")
            buildConfigField("String", "PUBLIC_BASE_URL", "\"${providers.gradleProperty("tapvizDebugPublicBaseUrl").getOrElse("https://tapviz.vorlan.ru")}\"")
        }
        release {
            optimization {
                enable = false
            }
            buildConfigField("String", "API_BASE_URL", "\"${providers.gradleProperty("tapvizApiBaseUrl").getOrElse("https://tapviz.vorlan.ru")}\"")
            buildConfigField("String", "PUBLIC_BASE_URL", "\"${providers.gradleProperty("tapvizPublicBaseUrl").getOrElse("https://tapviz.vorlan.ru")}\"")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.ui.text.googlefonts)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.zxing.core)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
