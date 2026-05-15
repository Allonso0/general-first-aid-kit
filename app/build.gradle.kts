import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)

    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)

    id("com.google.gms.google-services")
    id("jacoco")
}

android {
    namespace = "com.example.general_first_aid_kit"
    compileSdk = 36

    packaging {
        resources {
            excludes += "/META-INF/versions/9/OSGI-INF/MANIFEST.MF"
            excludes += "/META-INF/DEPENDENCIES"
            excludes += "/META-INF/LICENSE*"
            excludes += "/META-INF/NOTICE*"
        }
    }

    defaultConfig {
        applicationId = "com.example.general_first_aid_kit"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val properties = Properties()
        val localPropertiesFile = project.rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            properties.load(localPropertiesFile.inputStream())
        }

        val sbUrl = properties.getProperty("SUPABASE_URL") ?: ""
        val sbKey = properties.getProperty("SUPABASE_KEY") ?: ""
        val gigachatCreds = properties.getProperty("GIGACHAT_CREDENTIALS") ?: ""
        val eandbToken = properties.getProperty("EANDB_TOKEN") ?: ""

        buildConfigField("String", "SUPABASE_URL", "\"$sbUrl\"")
        buildConfigField("String", "SUPABASE_KEY", "\"$sbKey\"")
        buildConfigField("String", "GIGACHAT_CREDENTIALS", "\"$gigachatCreds\"")
        buildConfigField("String", "EANDB_TOKEN", "\"$eandbToken\"")
    }

    buildTypes {
        debug {
            enableUnitTestCoverage = true
        }
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
        buildConfig = true
        compose = true
    }
}

tasks.register<JacocoReport>("jacocoDomainReport") {
    dependsOn("testDebugUnitTest")
    group = "verification"

    reports {
        xml.required.set(true)
        html.required.set(true)
    }

    val classDir = fileTree("${layout.buildDirectory.get()}/tmp/kotlin-classes/debug") {
        include("**/domain/usecase/**", "**/domain/util/**")
        exclude("**/domain/model/**", "**/domain/repository/**")
    }

    sourceDirectories.setFrom(files("${projectDir}/src/main/java"))
    classDirectories.setFrom(classDir)
    executionData.setFrom(
        fileTree(layout.buildDirectory.get()) {
            include(
                "outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec",
                "jacoco/testDebugUnitTest.exec"
            )
        }
    )
}

dependencies {
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.logging.interceptor)

    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.barcode.scanning)
    implementation(libs.guava)

    implementation(libs.androidx.core.splashscreen)

    implementation(platform(libs.supabase.bom))
    implementation(libs.supabase.storage)
    implementation(libs.ktor.client.cio)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.storage)
    implementation(libs.firebase.messaging)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.hilt.work)
    ksp(libs.androidx.hilt.compiler)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    implementation(libs.fuzzywuzzy)

    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.hilt.android)
    implementation(libs.androidx.core.animation)
    implementation(libs.androidx.compose.animation.graphics)
    ksp(libs.hilt.android.compiler)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.coil.compose)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.arch.core.testing)

    androidTestImplementation(libs.mockk.android)
    androidTestImplementation(libs.hilt.android.testing)
    kspAndroidTest(libs.hilt.android.compiler)
    androidTestImplementation(libs.androidx.room.testing)
    androidTestImplementation(libs.androidx.work.testing)
    androidTestImplementation(libs.kotlinx.coroutines.test)
}