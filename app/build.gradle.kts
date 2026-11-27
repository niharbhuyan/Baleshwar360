import java.util.Properties
import java.io.FileInputStream
import java.util.Base64
import java.time.Duration

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.example"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.niharsales.baleshwar360"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val envProperties = Properties()
        listOf(rootProject.file(".env.example"), rootProject.file(".env")).forEach { candidate ->
            if (candidate.exists()) {
                FileInputStream(candidate).use { stream ->
                    envProperties.load(stream)
                }
            }
        }

        fun isPlaceholderSecret(value: String): Boolean {
            val v = value.trim()
            return v.isEmpty() ||
                v.startsWith("YOUR_", ignoreCase = true) ||
                v.startsWith("MY_", ignoreCase = true) ||
                v.contains("PLACEHOLDER", ignoreCase = true) ||
                v.contains("REPLACE_ME", ignoreCase = true) ||
                v.equals("AIzaSyBalasore360MapsKey", ignoreCase = true) ||
                v.equals("AIzaSyBiQAArK-Hc0lYU35S41zBvPQnZT1S0d0k", ignoreCase = true)
        }

        fun resolveSecret(vararg names: String): String {
            for (name in names) {
                val fromEnv = System.getenv(name)?.trim()?.trim('"', '\'').orEmpty()
                if (!isPlaceholderSecret(fromEnv)) return fromEnv
            }
            for (name in names) {
                val fromFile = envProperties.getProperty(name)?.trim()?.trim('"', '\'').orEmpty()
                if (!isPlaceholderSecret(fromFile)) return fromFile
            }
            return ""
        }

        fun escapeBuildConfig(value: String): String =
            value.replace("\\", "\\\\").replace("\"", "\\\"")

        val geminiKey = resolveSecret("GEMINI_API_KEY")
        buildConfigField("String", "GEMINI_API_KEY", "\"${escapeBuildConfig(geminiKey)}\"")

        val openWeatherKey = resolveSecret("OPENWEATHERMAP_API_KEY", "OPENWEATHER_API_KEY")
        buildConfigField("String", "OPENWEATHERMAP_API_KEY", "\"${escapeBuildConfig(openWeatherKey)}\"")

        val newsApiKey = resolveSecret("NEWS_API_KEY", "NEWSAPI_KEY")
        buildConfigField("String", "NEWS_API_KEY", "\"${escapeBuildConfig(newsApiKey)}\"")

        val balasoreDbPath = resolveSecret("BALASORE_DB_PATH")
            .ifBlank { "balasore_database" }
        buildConfigField("String", "BALASORE_DB_PATH", "\"${escapeBuildConfig(balasoreDbPath)}\"")

        val googleServicesFile = file("google-services.json")
        val firebaseConfigXml = file("src/main/res/values/firebase_applet_config.xml")
        val googleServicesText = if (googleServicesFile.exists()) googleServicesFile.readText() else ""
        val autoFirebaseProjectId = Regex("\"project_id\"\\s*:\\s*\"([^\"]+)\"")
            .find(googleServicesText)
            ?.groupValues
            ?.getOrNull(1)
            ?.trim()
            .orEmpty()
        val firebaseAutoConfigured = (googleServicesFile.exists() && firebaseConfigXml.exists()) ||
            autoFirebaseProjectId.isNotBlank()
        buildConfigField("String", "FIREBASE_PROJECT_ID", "\"${escapeBuildConfig(autoFirebaseProjectId)}\"")
        buildConfigField("boolean", "FIREBASE_AUTO_CONFIGURED", firebaseAutoConfigured.toString())

        val autoGoogleServicesKey = Regex("\"current_key\"\\s*:\\s*\"([^\"]+)\"")
            .find(googleServicesText)
            ?.groupValues
            ?.getOrNull(1)
            ?.trim()
            .orEmpty()

        val mapsApiKey = resolveSecret("MAPS_API_KEY", "GOOGLE_MAPS_API_KEY", "GOOGLE_MAPS_KEY")
            .ifBlank { autoGoogleServicesKey }
        manifestPlaceholders["MAPS_API_KEY"] = mapsApiKey
        buildConfigField("String", "MAPS_API_KEY", "\"${escapeBuildConfig(mapsApiKey)}\"")
    }

    val uploadKeystoreFile = file("${rootDir}/my-upload-key.jks")
    val uploadKeystoreBase64File = file("${rootDir}/my-upload-key.jks.base64")
    if (!uploadKeystoreFile.exists() && uploadKeystoreBase64File.exists()) {
        try {
            val decoded = Base64.getDecoder().decode(uploadKeystoreBase64File.readText().trim())
            uploadKeystoreFile.writeBytes(decoded)
        } catch (_: Exception) {}
    }

    signingConfigs {
        create("release") {
            storeFile = uploadKeystoreFile
            storePassword = "android"
            keyAlias = "upload"
            keyPassword = "android"
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    lint {
        abortOnError = false
        checkReleaseBuilds = false
    }
    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.play.services.ads)
    implementation(libs.play.app.update)
    implementation(libs.play.app.update.ktx)
    implementation(libs.coil.compose)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.moshi)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.moshi)
    implementation(libs.moshi.kotlin)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.messaging)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
    implementation(libs.play.services.maps)
    implementation(libs.kotlinx.coroutines.play.services)
    implementation(libs.mlkit.barcode.scanning)
    // Unused CameraX dependencies commented out to speed up builds and reduce APK size
    // implementation(libs.camera.camera2)
    // implementation(libs.camera.lifecycle)
    // implementation(libs.camera.view)
    implementation(libs.play.services.code.scanner)
    implementation(libs.osmdroid.android)
    implementation(libs.zxing.core)

    testImplementation(libs.junit)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}

tasks.withType<Test> {
    failFast = true
    timeout.set(Duration.ofMinutes(2))
    testLogging {
        events("passed", "failed", "skipped")
    }
}

tasks.configureEach {
    if (name.startsWith("merge") && name.contains("Dex", ignoreCase = true)) {
        outputs.doNotCacheIf("Avoid tar-packing large merged DEX outputs into local build cache") { true }
    }
}

