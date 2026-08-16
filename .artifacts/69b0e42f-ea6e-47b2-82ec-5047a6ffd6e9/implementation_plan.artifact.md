# Fix crash and theme reset when starting playback

The user reports that starting a song sometimes causes the app to switch to a dark theme and return to the main screen instead of playing music. This is likely caused by a `ForegroundServiceDidNotStartInTimeException` in `PlaybackService`, which leads to an app crash and subsequent restart. When the app restarts, it follows the system theme (which might be dark) and returns to the home screen (redirect from `LoginActivity`).

## Proposed Changes

### [Component] Playback Service

#### [MODIFY] [PlaybackService.kt](file:///home/mcsilk/AndroidStudioProjects/Sonus/app/src/main/java/com/example/sonus/PlaybackService.kt)
- Call `startForeground` immediately in `onStartCommand` with a placeholder notification to satisfy Android's foreground service requirements.
- Update the notification once the cover image is loaded via Glide.

### [Component] Player State & UI Helpers

#### [MODIFY] [PlayerState.kt](file:///home/mcsilk/AndroidStudioProjects/Sonus/app/src/main/java/com/example/sonus/PlayerState.kt)
- Add a method to remove state listeners to prevent memory leaks.
- Ensure only one instance of the same listener is added.

#### [MODIFY] [MiniPlayerHelper.kt](file:///home/mcsilk/AndroidStudioProjects/Sonus/app/src/main/java/com/example/sonus/MiniPlayerHelper.kt)
- Fix memory leak by ensuring the recurring progress update task is managed correctly (e.g., by associating it with the activity lifecycle or using a more stable approach).
- *Note: Since this is a static helper, I will modify it to return a cleanup function or use a more lifecycle-aware approach.*

#### [MODIFY] [PlayerActivity.kt](file:///home/mcsilk/AndroidStudioProjects/Sonus/app/src/main/java/com/example/sonus/PlayerActivity.kt)
- Remove the state listener in `onDestroy` to prevent memory leaks.

## Verification Plan

### Automated Tests
- N/A (UI and Service lifecycle related)

### Manual Verification
- Deploy the app to a device/emulator.
- Start a song from `AlbumDetailActivity`.
- Verify that the notification appears immediately and the app does not crash even on slow network connections.
- Verify that navigating between activities multiple times does not lead to OOM or multiple concurrent UI updates in the background.
