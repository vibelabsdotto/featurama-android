# Release preparation

This repository stages a release candidate locally. It has no remote publishing repository, upload task, signing credentials, or automatic release workflow. Do not interpret local staging as publication to Maven Central, GitHub Packages, or a production service.

## Build and verify

Use the checked-in Gradle 8.5 wrapper with JDK 17 or 21, Android SDK 34, and Build Tools 34.0.0. The build pins AGP 8.3.2, Kotlin 1.9.22, and Compose compiler 1.5.10. Keep those versions aligned; do not upgrade one merely to silence a version-availability warning.

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21) # macOS example
export ANDROID_HOME="$HOME/Library/Android/sdk"
./gradlew :featurama:build :featurama:lintRelease \
  :sample:assembleDebug :sample:assembleRelease :sample:lint \
  --max-workers=2 --console=plain
```

All existing debug and release unit tests must pass. Inspect both lint reports under `featurama/build/reports/`, not only the task exit code. `ObsoleteLintCustomCheck` is fatal so a skipped dependency registry fails verification. Dependency-update notices are not suppressions and do not justify a broad dependency upgrade.

Run real SDK security, cancellation, identity-race, and API readback checks against an authorized fixture. Keep API keys and dashboard cookies outside tracked files, sources, reports, and release artifacts. Do not enable verbose HTTP/header logging. Verify both the native Compose screen and a minified consuming app on an emulator at API 21, the supported minimum. Physical Android testing is optional when emulator coverage is the agreed scope.

## Stage the exact candidate locally

`GROUP` and `VERSION_NAME` in `gradle.properties` define the coordinates. The current candidate is `io.featurama:featurama-android:1.0.0`; this does not assert availability in a public repository. Select an unused version before any future remote release.

```bash
./gradlew :featurama:publishReleasePublicationToLocalReleaseRepository \
  --max-workers=2 --console=plain
```

Despite the Gradle task name, the only destination is this checkout's `build/local-maven/`. It does not write `~/.m2` and does not upload anything. Expected candidate files:

```text
build/local-maven/io/featurama/featurama-android/1.0.0/
  featurama-android-1.0.0.aar
  featurama-android-1.0.0.pom
  featurama-android-1.0.0.module
  featurama-android-1.0.0-sources.jar
  featurama-android-1.0.0-javadoc.jar
```

Gradle also writes checksums and Maven metadata. The sources and generated Kotlin API documentation must contain the current public API, not an empty documentation JAR. The license is MIT. The POM and SCM URLs must refer to `https://github.com/vibelabsdotto/featurama-android`, matching this repository's origin. Inspect the generated metadata rather than assuming the source properties were used.

## Verify a clean consumer

Use a separate directory with no `includeBuild`, project dependency, file-based AAR dependency, or SDK source path. Restrict the SDK group to the isolated Maven repository so another cached or public release cannot satisfy the dependency:

```kotlin
// settings.gradle.kts in the disposable consumer
pluginManagement {
    repositories { google(); mavenCentral(); gradlePluginPortal() }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        exclusiveContent {
            forRepository {
                maven { url = uri("/absolute/path/to/featurama-android/build/local-maven") }
            }
            filter { includeGroup("io.featurama") }
        }
        google()
        mavenCentral()
    }
}
```

Use the [README host configuration](README.md#installation), including core-library desugaring. Compile a host reference to `FeaturamaScreen` with a `Color` argument, plus a typed `KSerializer<FeatureRequest> = FeatureRequest.serializer()`. Do not add direct Compose or serialization dependencies solely to hide missing SDK API exports. Add normal host dependencies only when the app itself needs them, such as `activity-ktx` for a `ComponentActivity`.

Build debug and R8-minified release APKs, run lint, and record the resolved SDK coordinate, AAR file path, and SHA-256. Compare that hash with the staged AAR. Also verify POM-only resolution because some repositories and tools do not use Gradle Module Metadata. Removing core-library desugaring must fail the consumer's AAR metadata check, rather than allowing an API 21 crash.

Install the locally signed verification APK on an isolated emulator using an explicit ADB serial. A debug signing key is for emulator verification only, not app distribution. Read back project configuration, a known approved request, and its developer comment through the installed SDK. This exercises the packaged HTTP/serialization dependencies and desugared `Instant` on API 21. Open the Compose board and verify requests and comments render without `AndroidRuntime` failures. Do not put fixture credentials in the AAR or consumer source.

## Before a separately authorized remote release

The local artifact verification is complete only when all checks above pass. Actual publication remains a separate operation requiring explicit approval and the destination account:

- Confirm namespace ownership, destination repository, and unused version.
- Configure the approved repository and obtain its credentials through the appropriate secret mechanism. Never commit them.
- If the destination requires signatures, provide the release signing key and generate detached signatures for that exact candidate. No key or signatures are implied by local staging.
- Rebuild and repeat consumer/hash checks if source, metadata, dependencies, or the version changes.
- Verify the remote coordinates and download the exact artifact only after an authorized upload. Do not claim publication from a local Gradle success.

The default service is `https://newapi.featurama.app`. Old keys remain paired with their issuing origin. See [API key and origin pairing](README.md#api-key-and-origin-pairing); release preparation must not add automatic fallback to the legacy service.

## Official references

- [AGP 8.3 compatibility](https://developer.android.com/build/releases/past-releases/agp-8-3-0-release-notes), API 34, Gradle 8.4 minimum, JDK 17 minimum.
- [Gradle 8.5 Java 21 support](https://docs.gradle.org/8.5/release-notes.html).
- [Compose compiler and Kotlin compatibility](https://developer.android.com/jetpack/androidx/releases/compose-kotlin), compiler 1.5.10 with Kotlin 1.9.22.
- [Android dependency configurations](https://developer.android.com/build/dependencies), `api` versus `implementation` and bundled `lint.jar` checks.
- [Publication variants and metadata](https://developer.android.com/build/publish-library/configure-pub-variants).
- [Core-library desugaring](https://developer.android.com/studio/write/java8-support).
