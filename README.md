# Featurama Android SDK

Official Android SDK for [Featurama](https://featurama.io) - Feature Request Management for Mobile Apps.

> Status: Coming soon for MVP. React Native / Expo is currently the production-ready SDK.

## Installation

### Gradle (Kotlin DSL)

```kotlin
dependencies {
    implementation("io.featurama:featurama-android:1.0.0")
}
```

### Gradle (Groovy)

```groovy
dependencies {
    implementation 'io.featurama:featurama-android:1.0.0'
}
```

## Quick Start

### 1. Initialize the SDK

Initialize Featurama in your `Application` class:

```kotlin
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()

        Featurama.init("fm_live_your_api_key_here") {
            // Optional: Set a default user identifier
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

    // Vote for a feature request
    try {
        Featurama.vote(newRequest.id)
    } catch (e: ConflictException) {
        // User has already voted
    }

    // Remove a vote
    Featurama.removeVote(newRequest.id)
}
```

## Configuration Options

```kotlin
Featurama.init("fm_live_your_api_key_here") {
    // Your Convex deployment URL (defaults to https://featurama.convex.site)
    baseUrl("https://your-app.convex.site")

    // Default user identifier for votes and submissions
    defaultUserIdentifier("user_123")

    // Timeout settings (in milliseconds)
    connectTimeout(30_000)
    readTimeout(30_000)
    writeTimeout(30_000)
}
```

## API Reference

### Get Feature Requests

```kotlin
val response = Featurama.getFeatureRequests(
    page = 1,           // Page number (1-indexed)
    pageSize = 20       // Items per page (1-100)
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
    description = "Detailed description",     // Optional (max 2000 chars)
    submitterIdentifier = "user_123"          // Optional
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
    val createdAt: Instant
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
    .baseUrl("https://your-app.convex.site")
    .defaultUserIdentifier("user_123")
    .build()

val client = FeaturamaClient(config)

// Use the client
val requests = client.getFeatureRequests()
```

## Requirements

- Android SDK 21+ (Android 5.0 Lollipop)
- Kotlin 1.9+
- Java 8+

## Dependencies

The SDK uses:
- OkHttp 4.12 for HTTP requests
- kotlinx.serialization for JSON parsing
- Kotlin Coroutines for async operations

## License

MIT License - see [LICENSE](LICENSE) for details.
