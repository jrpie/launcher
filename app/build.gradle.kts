plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "de.jrpie.android.launcher"
    compileSdk = 35

    defaultConfig {
        applicationId = "de.jrpie.android.launcher"
        minSdk = 21
        targetSdk = 35
        versionCode = 46
        versionName = "0.2.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
    }

    val distributionDimension = "distribution"
    
    flavorDimensions += distributionDimension

    productFlavors {
        create("default") {
            dimension = distributionDimension
            isDefault = true
            buildConfigField("boolean", "USE_ACCESSIBILITY_SERVICE", "true")
        }
        create("accrescent") {
            dimension = distributionDimension
            applicationIdSuffix = ".accrescent"
            versionNameSuffix = "+accrescent"
            buildConfigField("boolean", "USE_ACCESSIBILITY_SERVICE", "false")
        }
    }

    sourceSets {
        this.getByName("accrescent") {
            this.java.srcDir("src/accrescent")
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
        buildConfig = true
        compose = true
        dataBinding = true
        viewBinding = true
    }

    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
    }

    lint {
        abortOnError = false
    }
}

dependencies {
    implementation(libs.androidx.activity)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.material)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.gridlayout)
    implementation(libs.androidx.palette.ktx)
    implementation(libs.androidx.preference.ktx)
    implementation(libs.androidx.recyclerview)
    implementation(libs.google.material)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.jonahbauer.android.preference.annotations)
    implementation(libs.androidx.ui.tooling.preview.android)
    kapt(libs.jonahbauer.android.preference.annotations)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.test.ext.junit)
}