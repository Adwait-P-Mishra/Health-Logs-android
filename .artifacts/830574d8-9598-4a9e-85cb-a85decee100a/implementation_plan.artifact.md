# Edge-to-Edge Navigation Padding Implementation Plan

The goal is to ensure the app's bottom padding adapts correctly based on the OS navigation mode (Gesture vs. Buttons). This involves using Jetpack Compose `WindowInsets` to automatically adjust spacing.

## User Review Required

> [!NOTE]
> The app already calls `enableEdgeToEdge()` in `MainActivity`, but several screens lack the necessary `WindowInsets` padding modifiers to handle the navigation bar height.

## Proposed Changes

### Editor Components

#### [MODIFY] [WorkoutEditorSheet.kt](file:///Users/adwait/Gym-Logs-android/app/src/main/java/com/adprmi/healthLogs/ui/components/WorkoutEditorSheet.kt)
- Add `navigationBarsPadding()` to the sticky bottom `Surface`.
- Increase the bottom `Spacer` in the `verticalScroll` column to account for the padded button area.

#### [MODIFY] [MealEditorSheet.kt](file:///Users/adwait/Gym-Logs-android/app/src/main/java/com/adprmi/healthLogs/ui/components/MealEditorSheet.kt)
- Add `navigationBarsPadding()` to the sticky bottom `Surface`.
- Increase the bottom `Spacer` in the `verticalScroll` column.

### Onboarding & Config Screens

#### [MODIFY] [AiOnboardingWelcomeScreen.kt](file:///Users/adwait/Gym-Logs-android/app/src/main/java/com/adprmi/healthLogs/ui/screens/AiOnboardingWelcomeScreen.kt)
- Apply `safeDrawingPadding()` to the root `Box` or the main `Column` to ensure content stays within safe areas.

#### [MODIFY] [AiConfigScreen.kt](file:///Users/adwait/Gym-Logs-android/app/src/main/java/com/adprmi/healthLogs/ui/screens/AiConfigScreen.kt)
- Apply `navigationBarsPadding()` to the `AiConnectionSuccessContent` Column.

### Core Screens

#### [MODIFY] [DashboardContent.kt](file:///Users/adwait/Gym-Logs-android/app/src/main/java/com/adprmi/healthLogs/ui/screens/DashboardContent.kt)
- Verify `innerPadding` usage to ensure the background bleeds into the navigation area if intended, while content remains safe.

## Verification Plan

### Automated Tests
- Run existing UI tests to ensure no regressions in layout.
- `./gradlew assembleDebug` to verify compilation.

### Manual Verification
- Deploy to a device/emulator.
- Toggle between **Gesture Navigation** and **3-Button Navigation** in Android Settings.
- Verify that the bottom "Save" buttons and list content are not obscured and have appropriate padding in both modes.
