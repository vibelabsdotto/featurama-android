# Featurama Android SDK

Official Android SDK for [Featurama](https://featurama.app) for feature requests, moderation, votes, and comments.

> This checkout implements the current public API and Compose board. Version `1.0.0` is the local release candidate coordinate, not a claim that this checkout is available on Maven Central. Building and local staging do not upload a release. See [release preparation](RELEASING.md).

## Installation

Enable `google()` and `mavenCentral()` in your host project's dependency repositories. For an unpublished candidate, add the isolated local Maven directory described in [release preparation](RELEASING.md). Do not consume a bare AAR: Maven metadata supplies the SDK's dependencies.

### Gradle (Kotlin DSL)

```kotlin
android {
    defaultConfig { minSdk = 21 }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions { jvmTarget = "1.8" }
    // Needed when calling FeaturamaScreen from Compose code.
    buildFeatures { compose = true }
    composeOptions { kotlinCompilerExtensionVersion = "1.5.10" } // Kotlin 1.9.22
}

dependencies {
    implementation("io.featurama:featurama-android:1.0.0")
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")
}
```

### Gradle (Groovy)

```groovy
android {
    defaultConfig { minSdk 21 }
    compileOptions {
        coreLibraryDesugaringEnabled true
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }
    kotlinOptions { jvmTarget = '1.8' }
    buildFeatures { compose true }
    composeOptions { kotlinCompilerExtensionVersion '1.5.10' } // Kotlin 1.9.22
}

dependencies {
    implementation 'io.featurama:featurama-android:1.0.0'
    coreLibraryDesugaring 'com.android.tools:desugar_jdk_libs:2.0.4'
}
```

Core-library desugaring is required in the consuming app, not just the SDK. It supplies `java.time.Instant` on API 21-25, and the AAR metadata enforces desugaring. Compose runtime/UI types and model serialization interfaces are exported as API dependencies. Declare any other libraries your own app imports, such as Material3 or activity-compose, in the app itself.

## Quick Start

### 1. Initialize the SDK

Initialize Featurama in your `Application` class:

```kotlin
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()

        Featurama.init("fm_live_your_api_key_here") {
            // Stable identity, required for submissions, votes and comments
            defaultUserIdentifier("user_${getUserId()}")
        }
    }
}
```

### 2. Use the SDK

All SDK methods are suspend functions and should be called from a coroutine:

```kotlin
// In a ViewModel or Activity
viewModelScope.launch {
    // Get feature requests
    val requests = Featurama.getFeatureRequests(page = 1, pageSize = 20)

    // Create a new feature request
    val newRequest = Featurama.createFeatureRequest(
        title = "Dark Mode",
        description = "Please add dark mode support"
    )

    // New requests are pending moderation and include the submitter's first vote.
    // Only approved requests allow vote changes.
    val approved = requests.items.firstOrNull { it.isApproved }
    if (approved != null) Featurama.toggleVote(approved.id)
}
```

## Configuration Options

```kotlin
Featurama.init("fm_live_your_api_key_here") {
    // Your Featurama API URL (defaults to https://newapi.featurama.app)
    baseUrl("https://your-api.example.com")

    // Default user identifier for votes and submissions
    defaultUserIdentifier("user_123")

    // Timeout settings (in milliseconds)
    connectTimeout(30_000)
    readTimeout(30_000)
    writeTimeout(30_000)
}
```

## API key and origin pairing

The default is `https://newapi.featurama.app`. Use a key issued by that service for that project. A legacy key issued by `https://api.featurama.app` remains paired with the legacy origin; it is not interchangeable with a new-service key. Local and self-hosted keys likewise belong to the origin that issued them.

For an intentional legacy connection, set the origin explicitly:

```kotlin
Featurama.init(legacyApiKey) {
    baseUrl("https://api.featurama.app")
}
```

This SDK targets the current API contract. Legacy-server support for newer moderation, config, and comment operations is not guaranteed. Migrate the project and obtain a current-service key to use the default. A 401 must be fixed by checking the key/origin pair, not by trying the same key against other hosts. The SDK never falls back to the legacy origin, and it does not follow redirects with API credentials.

## API Reference

### Get Feature Requests

```kotlin
val response = Featurama.getFeatureRequests(
    page = 1,           // Page number (1-indexed)
    pageSize = 20,      // Items per page (1-100)
    filter = "new"     // new, planned, in_progress, done, or null
)

// Response properties
response.items          // List<FeatureRequest>
response.page           // Current page
response.pageSize       // Items per page
response.totalCount     // Total number of requests
response.totalPages     // Total pages (calculated)

// Pagination helpers
response.hasNextPage
response.hasPreviousPage
response.isFirstPage
response.isLastPage
response.isEmpty
```

### Create Feature Request

```kotlin
val request = Featurama.createFeatureRequest(
    title = "New Feature",                    // Required (1-200 chars)
    description = "Detailed description",     // Required (max 2000 chars)
    submitterIdentifier = "user_123",         // Optional only if a default is set
    email = "user@example.com",              // Required when project config says required
    deviceInfo = DeviceInfo(platform = "Android", appVersion = "1.0")
)
```

### Update Feature Request

Only the original submitter can update a feature request:

```kotlin
val updated = Featurama.updateFeatureRequest(
    id = UUID.fromString("..."),
    title = "Updated Title",
    description = "Updated description",
    submitterIdentifier = "user_123"  // Must match original submitter
)
```

### Vote / Remove Vote

```kotlin
// Vote for a feature request
try {
    val updated = Featurama.vote(
        featureRequestId = UUID.fromString("..."),
        voterIdentifier = "user_123"  // Optional if default set
    )
    println("New vote count: ${updated.voteCount}")
} catch (e: ConflictException) {
    println("Already voted!")
}

// Remove a vote
val updated = Featurama.removeVote(
    featureRequestId = UUID.fromString("..."),
    voterIdentifier = "user_123"
)
```

### Update User Identifier

```kotlin
// After user login
Featurama.setUserIdentifier("logged_in_user_id")

// After user logout
Featurama.setUserIdentifier(null)
```

## Compose board

```kotlin
setContent {
    MaterialTheme {
        FeaturamaScreen(onClose = { finish() })
    }
}
```

The board includes pagination, votes, moderation state, author editing, comments, and email collection from project config. Without a configured user, it creates a persistent anonymous ID local to the app. It submits permission-free device diagnostics with new requests. It does not read hardware identifiers, contacts, or location.

## Comments and project config

```kotlin
val config = Featurama.getProjectConfig()
// config.emailCollection: none, optional, required
// config.branding.showBranding controls the footer.
val comments = Featurama.getComments(requestId)
val comment = Featurama.addComment(requestId, "This would help", authorName = "Alex")
Featurama.voteComment(requestId, comment.id)
Featurama.removeCommentVote(requestId, comment.id)
Featurama.toggleCommentVote(requestId, comment.id)
```

The API returns an anonymized author identifier for comments, not an account identity. It does not expose comment `hasVoted`, so the UI labels comment votes as a toggle rather than claiming persisted selected state. Request listings include `hasVoted` and the current user's pending requests when a user identifier is set. Public listings redact email, device details, and other submitters' identifiers. The create response can include the diagnostics and email just submitted.

## Build and run the sample

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export ANDROID_HOME="$HOME/Library/Android/sdk"
./gradlew :featurama:build :sample:assembleDebug
```

The sample reads `FEATURAMA_API_KEY` and optional `FEATURAMA_BASE_URL` from the build environment. Keep real values outside tracked files. The default URL is `https://newapi.featurama.app`; `api.featurama.app` is the legacy service.

For a local API on Android Emulator, set `FEATURAMA_BASE_URL=http://localhost:3000`, run `adb -s <serial> reverse tcp:3000 tcp:3000`, and build/install the debug sample. Cleartext HTTP is enabled only in the debug sample manifest. The sample contains both a direct-client form and an "Open feature board" button. Loading requests never votes on a user's behalf.

## Error Handling

The SDK throws specific exceptions for different error cases:

```kotlin
try {
    Featurama.getFeatureRequests()
} catch (e: UnauthorizedException) {
    // 401 - Invalid API key
} catch (e: ForbiddenException) {
    // 403 - Access denied (e.g., trying to update someone else's request)
} catch (e: NotFoundException) {
    // 404 - Resource not found
} catch (e: ConflictException) {
    // 409 - Conflict (e.g., already voted)
} catch (e: ServerException) {
    // 5xx - Server error
} catch (e: NetworkException) {
    // Network error (no connection, timeout)
} catch (e: FeaturamaException) {
    // Other SDK errors
}
```

## Models

### FeatureRequest

```kotlin
data class FeatureRequest(
    val id: UUID,
    val projectId: UUID,
    val title: String,
    val description: String?,
    val status: FeatureRequestStatus,
    val source: FeatureRequestSource,
    val voteCount: Int,
    val submitterIdentifier: String?,
    val createdAt: Instant,
    val isApproved: Boolean,
    val hasVoted: Boolean,
    val commentCount: Int,
    val submitterEmail: String?,
    val deviceInfo: DeviceInfo?
)
```

### FeatureRequestStatus

```kotlin
enum class FeatureRequestStatus {
    REQUESTED,    // Initial state when created via SDK
    ROADMAP,      // Planned for future implementation
    IN_PROGRESS,  // Currently being worked on
    DONE,         // Completed
    DECLINED      // Will not be implemented
}
```

### FeatureRequestSource

```kotlin
enum class FeatureRequestSource {
    DASHBOARD,  // Created via web dashboard
    SDK         // Created via SDK
}
```

## Advanced Usage

### Using FeaturamaClient Directly

For more control, you can create and manage `FeaturamaClient` instances directly:

```kotlin
val config = FeaturamaConfig.Builder("fm_live_your_api_key")
    .baseUrl("https://api.example.com")
    .defaultUserIdentifier("user_123")
    .build()

val client = FeaturamaClient(config)

// Use the client
val requests = client.getFeatureRequests()
```

## Requirements

- Android SDK 21+ (Android 5.0 Lollipop)
- Kotlin 1.9.22 with Compose compiler 1.5.10 for this checkout. Host apps must use a [compatible compiler/Kotlin pair](https://developer.android.com/jetpack/androidx/releases/compose-kotlin).
- Android Gradle Plugin 8.3.2+, compileSdk 34+, and core-library desugaring 2.0.4+. The AAR enforces the AGP and desugaring requirements.
- Java 8 bytecode. Keep core-library desugaring enabled in the host app; `java.time` needs it on API 21-25.
- Use JDK 17 or 21 to build this checkout with its Gradle 8.5 wrapper.
- Compose remains at 1.6.0 with Material3 1.2.0. AGP 8.3.2 loads their custom lint checks; AGP 8.2.2 silently skipped the Compose runtime registry. Skipped custom-check registries are now fatal in SDK lint.

## Dependencies

The SDK uses:
- OkHttp 4.12 for HTTP requests
- kotlinx.serialization for JSON parsing
- Kotlin Coroutines for async operations

## License

MIT License - see [LICENSE](LICENSE) for details.
