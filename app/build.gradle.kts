plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}


// Remove stale logo files left behind by older repository uploads.
// This runs before Android resource merging, so old ic_sahana_logo.xml
// cannot collide with the current SAHANA launcher PNG.
val cleanupSahanaLegacyLogo by tasks.registering {
    doLast {
        val resRoot = file("src/main/res")
        resRoot.walkTopDown()
            .filter { it.isFile && (it.name == "ic_sahana_logo.xml" || it.name == "ic_sahana_logo.svg" || it.name == "ic_sahana_logo.png") }
            .forEach {
                println("Removing stale SAHANA logo: ${it.relativeTo(projectDir)}")
                it.delete()
            }
    }
}

tasks.named("preBuild").configure {
    dependsOn(cleanupSahanaLegacyLogo)
}

android {
    namespace = "com.sahana.expense"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.sahana.expense"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    // TEST RELEASE: sign the APK so it can be installed directly.
    // Replace with a private release keystore before Play Store publishing.
    signingConfigs {
        getByName("debug")
    }

    buildTypes {
        getByName("release") {
            signingConfig = signingConfigs.getByName("debug")
            isMinifyEnabled = false
        }
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.compose.ui:ui:1.7.8")
    implementation("androidx.compose.ui:ui-tooling-preview:1.7.8")
    implementation("androidx.compose.material3:material3:1.3.1")
    implementation("androidx.compose.material:material-icons-extended:1.7.8")
    implementation("androidx.documentfile:documentfile:1.0.1")
    implementation("androidx.exifinterface:exifinterface:1.3.7")
    implementation("androidx.fragment:fragment-ktx:1.8.5")
    implementation("androidx.biometric:biometric:1.1.0")
}
