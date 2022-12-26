/*
 * Copyright 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

plugins {
    id("com.android.library")
    id("kotlin-android")
    id("org.jetbrains.dokka")
    id("dev.chrisbanes.paparazzi")
    id("me.tylerbwong.gradle.metalava")
}

android {
    compileSdk = 33

    defaultConfig {
        minSdk = 26
        targetSdk = 30

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    buildFeatures {
        buildConfig = false
        compose = true
    }

    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs += "-opt-in=androidx.lifecycle.compose.ExperimentalLifecycleComposeApi"
        freeCompilerArgs += "-opt-in=com.google.accompanist.pager.ExperimentalPagerApi"
        freeCompilerArgs += "-opt-in=com.google.android.horologist.audio.ExperimentalHorologistAudioApi"
        freeCompilerArgs += "-opt-in=com.google.android.horologist.audio.ui.ExperimentalHorologistAudioUiApi"
        freeCompilerArgs += "-opt-in=com.google.android.horologist.base.ui.ExperimentalHorologistBaseUiApi"
        freeCompilerArgs += "-opt-in=com.google.android.horologist.composables.ExperimentalHorologistComposablesApi"
        freeCompilerArgs += "-opt-in=com.google.android.horologist.compose.navscaffold.ExperimentalHorologistComposeLayoutApi"
        freeCompilerArgs += "-opt-in=com.google.android.horologist.media.ExperimentalHorologistMediaApi"
        freeCompilerArgs += "-opt-in=com.google.android.horologist.tiles.ExperimentalHorologistTilesApi"
        freeCompilerArgs += "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi"
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
    }
    packagingOptions {
        resources {
            excludes += listOf(
                "/META-INF/AL2.0",
                "/META-INF/LGPL2.1"
            )
        }
    }


        sourceSets.getByName("main") {
            assets.srcDir("src/main/assets")
        }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
        animationsDisabled = true
    }
    lint {
        checkReleaseBuilds = false
        textReport = true
        disable.addAll(listOf("MissingTranslation", "ExtraTranslation"))
    }
    namespace = "com.google.android.horologist.media.ui"
}

project.tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    // Workaround for https://youtrack.jetbrains.com/issue/KT-37652
    if (!this.name.endsWith("TestKotlin") && !this.name.startsWith("compileDebug")) {
        this.kotlinOptions {
            freeCompilerArgs = freeCompilerArgs + "-Xexplicit-api=strict"
        }
    }
}

metalava {
    sourcePaths = mutableSetOf("src/main")
    filename = "api/current.api"
    reportLintsAsErrors = true
}

dependencies {
    api(projects.media)
    api(projects.tiles)
    api(projects.composables)
    implementation(projects.baseUi)
    implementation(projects.audio)
    implementation(projects.audioUi)
    implementation(projects.composeLayout)
    implementation(libs.kotlin.stdlib)
    implementation(libs.androidx.wear)
    implementation(libs.androidx.lifecycle.runtime)
    implementation(libs.androidx.lifecycle.viewmodelktx)
    api(libs.wearcompose.material)
    api(libs.wearcompose.foundation)
    implementation(libs.compose.ui.tooling)
    implementation(libs.compose.material.iconscore)
    implementation(libs.compose.material.iconsext)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    implementation(libs.coil)
    implementation(libs.lottie.compose)
    implementation(libs.androidx.palette.ktx)

    implementation(projects.tiles)
    implementation(projects.composeTools)
    implementation(libs.androidx.complications.datasource.ktx)
    implementation(libs.androidx.wear.tiles)
    implementation(libs.androidx.wear.tiles.material)
    implementation(libs.compose.ui.tooling)
    implementation(libs.compose.ui.util)

    debugImplementation(libs.compose.ui.test.manifest)
    debugImplementation(libs.compose.ui.toolingpreview)
    debugImplementation(projects.audioUi)
    debugImplementation(projects.composeTools)

    testImplementation(libs.junit)
    testImplementation(libs.androidx.test.ext.ktx)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.truth)
    testImplementation(libs.robolectric)
    testImplementation(projects.audio)
    testImplementation(projects.audioUi)
    testImplementation(projects.paparazzi)
    testImplementation(libs.paparazzi)

    androidTestImplementation(libs.compose.ui.test.junit4)
    androidTestImplementation(libs.truth)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext)
    androidTestImplementation(libs.androidx.test.ext.ktx)
}

apply(plugin = "com.vanniktech.maven.publish")