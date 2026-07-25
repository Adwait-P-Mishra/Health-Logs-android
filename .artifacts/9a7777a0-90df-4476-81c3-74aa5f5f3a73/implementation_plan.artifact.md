# Remove Non-Crucial Components Plan

The goal is to remove all files, code, and configurations that are not strictly necessary for the application to build and run, while maintaining all existing user-facing functionality.

## Proposed Changes

### [Cleanup] Project Assets and Documentation
Remove files that are for developer reference or play store assets but not used at runtime.

#### [DELETE] [screenshots](file:///C:/Users/adwai/Desktop/training-&-nutrition/app/src/main/assets/screenshots/)
#### [DELETE] [ic_launcher-playstore.png](file:///C:/Users/adwai/Desktop/training-&-nutrition/app/src/main/ic_launcher-playstore.png)
#### [DELETE] [assets/](file:///C:/Users/adwai/Desktop/training-&-nutrition/assets/) (Root assets directory containing only IDE metadata)

### [Cleanup] Testing Infrastructure
Remove all test code and related configurations as they are not needed for running the application.

#### [DELETE] [test/](file:///C:/Users/adwai/Desktop/training-&-nutrition/app/src/test/)
#### [DELETE] [androidTest/](file:///C:/Users/adwai/Desktop/training-&-nutrition/app/src/androidTest/)

### [Modify] Build Configuration
Clean up `build.gradle.kts` and `libs.versions.toml` to remove unused dependencies, plugins, and commented-out code.

#### [MODIFY] [app/build.gradle.kts](file:///C:/Users/adwai/Desktop/training-&-nutrition/app/build.gradle.kts)
- Remove `roborazzi` plugin and its configuration.
- Remove `secrets` plugin and its configuration (as it's not currently used by the code).
- Remove all commented-out dependencies.
- Remove test dependencies (since test folders are deleted).
- Remove `firebase-bom` (since no actual Firebase services are used).
- Remove `buildConfig = true` (not used).

#### [MODIFY] [build.gradle.kts](file:///C:/Users/adwai/Desktop/training-&-nutrition/build.gradle.kts)
- Remove unused plugin declarations (`roborazzi`, `secrets`).

#### [MODIFY] [gradle/libs.versions.toml](file:///C:/Users/adwai/Desktop/training-&-nutrition/gradle/libs.versions.toml)
- Remove versions and libraries that are no longer referenced.

## Verification Plan

### Automated Tests
- None (since tests are being removed).

### Manual Verification
- Run `./gradlew assembleDebug` to ensure the app still builds.
- Deploy the app to a device/emulator to ensure it still runs correctly and all screens are accessible.
- Verify that the Dashboard, Workout Logs, Meal Logs, and Settings (Import/Export) still work.
