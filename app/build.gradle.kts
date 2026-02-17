import java.io.ByteArrayOutputStream
import java.nio.charset.Charset
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion


plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.legacy.kapt)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    compilerOptions {
        languageVersion = KotlinVersion.fromVersion("2.3")
        jvmTarget = JvmTarget.fromTarget("17")
    }
}

abstract class GitCommitValueSource : ValueSource<String, ValueSourceParameters.None> {

    @Inject
    abstract fun getExecOperations(): ExecOperations

    override fun obtain(): String {
        val output = ByteArrayOutputStream()
        val action = object : Action<ExecSpec> {
            override fun execute(t: ExecSpec) {
                t.commandLine("git", "rev-parse", "--verify", "--short", "HEAD")
                t.standardOutput = output
            }
        }
        getExecOperations().exec(action)
        return String(output.toByteArray(), Charset.defaultCharset()).trim()
    }
}

val gitCommitProvider = providers.of(GitCommitValueSource::class) {}
val gitCommit = gitCommitProvider.get()

dependencies {
    implementation(libs.androidx.activity)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.gridlayout)
    implementation(libs.androidx.palette.ktx)
    implementation(libs.androidx.preference.ktx)
    implementation(libs.androidx.recyclerview)
    implementation(libs.google.material)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.jonahbauer.android.preference.annotations)
    annotationProcessor(libs.jonahbauer.android.preference.annotations)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.test.ext.junit)
}

android {
    namespace = "de.jrpie.android.launcher"
    compileSdk = 36

    defaultConfig {
        applicationId = "de.jrpie.android.launcher"
        minSdk = 21
        targetSdk = 36
        versionCode = 54
        versionName = "0.2.9"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    androidResources {
        generateLocaleConfig = true
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
            isDebuggable = true
        }
    }

    val distributionDimension = "distribution"
    flavorDimensions += distributionDimension

    defaultConfig {
        buildConfigField("String", "GIT_COMMIT", "\"${gitCommit}\"")
    }

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

    sourceSets.named("accrescent") {
        kotlin.directories += "src/accrescent"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        buildConfig = true
        compose = false
        dataBinding = true
        viewBinding = true
    }

    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
    }

    packaging {
        resources.excludes.addAll(
            listOf(
                "META-INF/LICENSE.md",
                "META-INF/NOTICE.md",
                "META-INF/LICENSE-notice.md"
            )
        )
    }

    lint {
        abortOnError = false
    }
}
