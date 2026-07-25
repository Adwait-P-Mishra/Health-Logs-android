# Remake App Icon and Feature Graphic (Nutrition + Training)

The user requested a new design based on an image that features both nutrition (fork and knife) and training (lifter) separated by a vertical line on a black background.

## User Review Required

> [!IMPORTANT]
> The new design uses a **Black Background** for the icon, which matches the provided image. This will change the look of the app on the home screen significantly compared to the previous white version.

## Proposed Changes

### Assets Design

I will create a complex Vector XML that includes:
1.  **Nutrition Symbols**: A fork and knife on the left.
2.  **Vertical Separator**: A line in the middle.
3.  **Training Symbol**: A stylized person performing a deadlift/squat with a barbell.

#### [MODIFY] [ic_launcher_foreground.xml](file:///C:/Users/adwai/Desktop/training-&-nutrition/app/src/main/res/drawable/ic_launcher_foreground.xml)
Replace the simple barbell with the new multi-element design. All elements will be white to contrast with the black background.

#### [MODIFY] [ic_launcher_background.xml](file:///C:/Users/adwai/Desktop/training-&-nutrition/app/src/main/res/drawable/ic_launcher_background.xml)
Change the fill color from `#FFFFFF` (White) to `#000000` (Black).

#### [MODIFY] [StoreAssetsPreview.kt](file:///C:/Users/adwai/Desktop/training-&-nutrition/app/src/main/java/com/adprmi/gymLogs/ui/StoreAssetsPreview.kt)
Update the previews to show the new icon and feature graphic. The icon preview will now use a black background.

## Verification Plan

### Manual Verification
- I will use `render_compose_preview` to show the updated designs.
- I will ensure the proportions match the provided reference image.
