# AGENTS.md

This file provides guidance to AI coding assistants when working with code in this repository.

## Project Overview

IronBeast Android is a legacy Android client SDK for event tracking and analytics reporting. It provides a lightweight mechanism for sending analytics and event data from Android applications to the IronBeast backend service. The SDK handles event queuing, batch processing, network connectivity management, and automatic retries with exponential backoff.

**Note**: This is a legacy SDK. The recommended alternative is: https://github.com/ironSource/atom-android

The application is built with Java for Android (minimum SDK 10, target SDK 22) and uses a Service-Oriented Architecture with background processing for event reporting.

## Development Commands

### Building

- `./gradlew assembleRelease` - Build release version and generate JAR
- `./gradlew assembleDebug` - Build debug version and generate JAR
- `./gradlew build` - Build all variants

### Testing

- `./gradlew test` - Run all unit tests
- `./gradlew testDebug` - Run tests for debug build
- `./gradlew testRelease` - Run tests for release build

### Code Quality

- `./gradlew lint` - Run linter on all files
- Note: `abortOnError` is set to false in lintOptions

### Cleaning

- `./gradlew clean` - Clean build artifacts
- `./gradlew clearJar` - Remove generated JAR files from output directory

## Architecture Overview

### Android SDK Structure

- **Language**: Java
- **Build System**: Gradle 2.1.2
- **Min SDK**: Android 10 (API level 10)
- **Target SDK**: Android 22 (API level 22)
- **Build Tools**: 22.0.1
- **Architecture Pattern**: Service-Oriented with background processing

### Key Directories

- `/ironbeastsdk/src/main/java/io/ironbeast/sdk/` - Main SDK source code
- `/ironbeastsdk/src/test/java/io/ironbeast/sdk/` - Unit tests
- `/output/` - Generated JAR files
- `/ironbeastsdk/mobileCore.pro` - ProGuard configuration

### Core Components

The SDK follows a modular design with clear separation of concerns:

- **IronBeast.java** - Main SDK entry point and initialization
- **IronBeastTracker.java** - Primary public API for tracking events
- **ReportService.java** - Background IntentService for event reporting
- **ReportHandler.java** - Handles report processing logic
- **DbAdapter.java** - SQLite database adapter for local event storage
- **StorageService.java** - Service layer for data persistence
- **NetworkManager.java** - Connectivity and network state management
- **HttpClient.java** - HTTP communication with backend service
- **RemoteService.java** - Remote API abstraction layer
- **BackOff.java** - Exponential backoff implementation for retries
- **IBConfig.java** - SDK configuration management
- **IBPrefService.java** - SharedPreferences management
- **Report.java** - Event report data model
- **SdkEvent.java** - SDK event representation
- **Utils.java** - Common utility functions
- **Logger.java** - Logging abstraction
- **Consts.java** - Application constants
- **ExternalVars.java** - External configuration variables
- **ReportIntent.java** - Intent handling for reports

### Service Architecture

The SDK uses Android's IntentService pattern for background event processing:

1. Events are tracked via `IronBeastTracker` API
2. Events are stored locally in SQLite database via `DbAdapter`
3. `ReportService` (IntentService) processes queued events in background
4. `NetworkManager` monitors connectivity and triggers reports
5. `HttpClient` sends events to backend with exponential backoff on failure
6. Successfully sent events are removed from local storage

## Code Standards

### Java Conventions

- Package structure: `io.ironbeast.sdk`
- Use clear, descriptive class and method names
- Follow Android SDK conventions for services and lifecycle management
- Implement proper error handling with try-catch blocks
- Use Android logging via `Logger` utility class
- Maintain backward compatibility with Android API 10+
- Document public API methods with Javadoc comments

### Build Variants

- **release** - Production build with ProGuard, points to production URLs
- **debug** - Development build with local URLs (localhost:3000)
- **debugGoogleEmulator** - Google emulator specific configuration (10.0.2.2:3000)
- **debugGenyMotionEmulator** - Genymotion emulator configuration (10.0.3.2:3000)

### Testing

- Testing Framework: JUnit 4.12
- Mocking: Mockito 1.10.19
- Android Testing: Robolectric 3.0-rc2
- Test files located in `ironbeastsdk/src/test/java/`
- Unit tests return default values for unmocked Android components
- Run specific test class: `./gradlew test --tests "io.ironbeast.sdk.ClassName"`
- Test coverage focuses on core SDK logic and data handling

## Key Configuration Files

- `build.gradle` (root) - Top-level build configuration, repositories
- `ironbeastsdk/build.gradle` - SDK module configuration, dependencies, build variants
- `ironbeastsdk/mobileCore.pro` - ProGuard rules for release builds
- `gradle.properties` - Gradle build properties
- `AndroidManifest.xml` - Android manifest with service declarations

## Database & External Dependencies

### Required Components

- SQLite database (Android built-in) for local event storage
- Network connectivity (Android connectivity services)
- HTTP client for backend communication

### Key External Integrations

- IronBeast backend service (sdk.ironbeast.io) for event reporting
- Bulk event endpoint (sdk.ironbeast.io/bulk) for batch uploads
- Local SQLite database for event persistence and offline support

### Testing Dependencies

- JUnit 4.12 for unit testing framework
- Mockito 1.10.19 for mocking objects
- Robolectric 3.0-rc2 for Android component testing
- JSON library (org.json:json:20090211) for JSON handling in tests

## Common Workflows

### Adding a New Event Type

1. Create or modify event data structure in `SdkEvent.java`
2. Update `IronBeastTracker` API if new tracking method needed
3. Ensure event serialization/deserialization works correctly
4. Test event storage in `DbAdapter` and retrieval
5. Verify event transmission via `HttpClient`

### Debugging Network Issues

1. Check network connectivity state via `NetworkManager`
2. Review `BackOff` retry logic and timing
3. Examine `HttpClient` request/response handling
4. Verify endpoint URLs in build configuration
5. Check `ReportService` background processing
6. Review logs via `Logger` utility

### Building and Distributing SDK

1. Update version in `ironbeastsdk/build.gradle` (versionName and versionCode)
2. Run `./gradlew assembleRelease` to build release JAR
3. Find output in `/output/IronBeastSDK_v[version]_release.jar`
4. For debug builds, use `./gradlew assembleDebug`
5. JAR files are automatically renamed and moved to output directory

### Modifying Build Variants

1. Edit build types in `ironbeastsdk/build.gradle`
2. Update buildConfigField values for URLs and feature flags
3. Adjust ProGuard rules if needed in `mobileCore.pro`
4. Test each variant separately
5. Ensure backward compatibility with supported Android versions

## Important Notes

- This is a **legacy SDK** - new projects should use atom-android instead
- Supports very old Android versions (minimum API 10)
- Uses older Android patterns (IntentService, SQLite directly)
- No Kotlin support
- Limited modern Android architecture patterns (no MVVM, Jetpack, etc.)
- Documentation available at: http://docs.ironbeast.io/sdk.html
- License: MIT
