import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    jvm("desktop")

    sourceSets {
        val desktopMain by getting

        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
        }

        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.multiplatform.settings)
            implementation(libs.multiplatform.settings.noarg)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.coroutines.core)
        }

        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}

android {
    namespace = "com.gomoku.nusv"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.gomoku.nusv"
        minSdk = 26
        targetSdk = 36
        versionCode = 8
        versionName = "1.4.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

compose.desktop {
    application {
        mainClass = "com.gomoku.nusv.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Deb)
            packageName = "Gomoku-NUSV"
            packageVersion = "1.4.1"
            description = "五子棋对弈应用 Gomoku-NUSV"
            vendor = "Gomoku"
        }
    }
}

abstract class ApplyAppIconTask : DefaultTask() {
    @get:InputFile
    abstract val icnsFile: RegularFileProperty

    @get:InputDirectory
    abstract val appDir: DirectoryProperty

    @TaskAction
    fun apply() {
        val contentsDir = appDir.get().asFile.resolve("Contents")
        val resourcesDir = contentsDir.resolve("Resources")
        icnsFile.get().asFile.copyTo(resourcesDir.resolve("icon.icns"), overwrite = true)
        val plist = contentsDir.resolve("Info.plist")
        runPlist("-c", "Delete :CFBundleIconFile", plist.absolutePath, ignoreError = true)
        runPlist("-c", "Add :CFBundleIconFile string icon.icns", plist.absolutePath)
        runProcess("codesign", "--force", "--sign", "-", "--deep", appDir.get().asFile.absolutePath)
    }

    private fun runPlist(vararg cmd: String, ignoreError: Boolean = false) {
        runProcess("/usr/libexec/PlistBuddy", *cmd, ignoreError = ignoreError)
    }

    private fun runProcess(vararg cmd: String, ignoreError: Boolean = false) {
        val process = ProcessBuilder(*cmd).redirectErrorStream(true).start()
        process.inputStream.bufferedReader().use { it.readLines() }
        process.waitFor()
        if (process.exitValue() != 0 && !ignoreError) {
            throw GradleException("Command failed: ${cmd.joinToString(" ")}")
        }
    }
}

val applyAppIcon = tasks.register<ApplyAppIconTask>("applyAppIcon") {
    dependsOn("createDistributable")
    icnsFile.set(layout.projectDirectory.file("src/desktopMain/resources/icon.icns"))
    appDir.set(layout.buildDirectory.dir("compose/binaries/main/app/Gomoku-NUSV.app"))
}

tasks.configureEach {
    if (name == "packageDmg" || name == "packageApp") {
        dependsOn(applyAppIcon)
    }
}
