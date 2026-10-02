plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("maven-publish")
}

android {
    namespace = "io.featurama.sdk"
    compileSdk = 34

    defaultConfig {
        minSdk = 21
        aarMetadata {
            // Older lint engines skip Compose's bundled runtime checks.
            minAgpVersion = "8.3.2"
        }

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.10"
    }

    lint {
        // A green task must not hide a skipped dependency issue registry.
        fatal += "ObsoleteLintCustomCheck"
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }
}

dependencies {
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")
    // Generated public model serializers return KSerializer.
    api("org.jetbrains.kotlinx:kotlinx-serialization-core:1.6.3")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // Public @Composable functions and theme colors are part of the SDK API.
    api("androidx.compose.runtime:runtime:1.6.0")
    api("androidx.compose.ui:ui:1.6.0")
    implementation("androidx.compose.material3:material3:1.2.0")
    implementation("androidx.compose.ui:ui-tooling-preview:1.6.0")
    implementation("androidx.compose.foundation:foundation:1.6.0")
    implementation("androidx.activity:activity-compose:1.8.2")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.0")
    testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")
    testImplementation("io.mockk:mockk:1.13.9")
}

val prepareLicenseResources by tasks.registering(Sync::class) {
    // Use a namespaced path so Android packaging does not strip the license.
    from(rootProject.file("LICENSE")) { into("META-INF/featurama") }
    into(layout.buildDirectory.dir("generated/licenseResources"))
}
android.sourceSets.getByName("main").resources.srcDir(prepareLicenseResources)
tasks.named("preBuild") { dependsOn(prepareLicenseResources) }

tasks.withType<Jar>().configureEach {
    from(rootProject.file("LICENSE")) { into("META-INF") }
}

publishing {
    repositories {
        // Local staging only. Remote publication requires a separate, explicit setup.
        maven {
            name = "localRelease"
            url = rootProject.layout.buildDirectory.dir("local-maven").get().asFile.toURI()
        }
    }

    publications {
        register<MavenPublication>("release") {
            groupId = project.findProperty("GROUP") as String
            artifactId = "featurama-android"
            version = project.findProperty("VERSION_NAME") as String

            afterEvaluate {
                from(components["release"])
            }

            pom {
                name.set(project.findProperty("POM_NAME") as String)
                description.set(project.findProperty("POM_DESCRIPTION") as String)
                url.set(project.findProperty("POM_URL") as String)

                licenses {
                    license {
                        name.set(project.findProperty("POM_LICENCE_NAME") as String)
                        url.set(project.findProperty("POM_LICENCE_URL") as String)
                    }
                }

                developers {
                    developer {
                        id.set(project.findProperty("POM_DEVELOPER_ID") as String)
                        name.set(project.findProperty("POM_DEVELOPER_NAME") as String)
                    }
                }

                scm {
                    url.set(project.findProperty("POM_SCM_URL") as String)
                    connection.set(project.findProperty("POM_SCM_CONNECTION") as String)
                    developerConnection.set(project.findProperty("POM_SCM_DEV_CONNECTION") as String)
                }
            }
        }
    }
}
