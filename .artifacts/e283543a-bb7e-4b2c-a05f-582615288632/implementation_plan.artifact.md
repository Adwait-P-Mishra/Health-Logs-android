# App-Wide Dark Mode Theme Refinement

This plan aims to resolve the "weird shift" in dark mode by adjusting the foreground colors (Primary, Secondary, Tertiary) to maintain a "colored background + light text" appearance, similar to the light mode, but optimized for visibility against a dark background.

## User Review Required

> [!IMPORTANT]
> The current Material 3 implementation uses light pastel colors for components in dark mode (e.g., light teal buttons with dark text). This plan will switch them to more saturated colors with white text.

## Proposed Changes

### Theme & Colors

#### [MODIFY] [Color.kt](file:///Users/adwait/Gym-Logs-android/app/src/main/java/com/adprmi/healthLogs/ui/theme/Color.kt)
- Add new dark-mode specific color constants that provide better contrast against dark backgrounds while supporting white text.
- Suggested additions:
    - `PrimaryDark`: A more vibrant teal than the light mode's `Primary`.
    - `SecondaryDark`: A more vibrant coral/orange.
    - `TertiaryDark`: A more vibrant brown/bronze.

#### [MODIFY] [Theme.kt](file:///Users/adwait/Gym-Logs-android/app/src/main/java/com/adprmi/healthLogs/ui/theme/Theme.kt)
- Update `DarkColorScheme` to use these new colors.
- Set `onPrimary`, `onSecondary`, and `onTertiary` to white/light colors to ensure the "classic" high-contrast look.
- Adjust `primaryContainer` and other container colors to align with this new direction.

## Verification Plan

### Automated Tests
- I will run `render_compose_preview` on key components like `AddButtonsRow` and `AiEstimateButton` in dark mode to verify the visual shift.

### Manual Verification
- The user should check the `MainActivity` in dark mode to see the effect across the entire app.
