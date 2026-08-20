# Fix SocketTimeoutException Crash

The application is experiencing a `FATAL EXCEPTION: main` due to an unhandled `java.net.SocketTimeoutException` when a network request fails (specifically when trying to connect to the Tailscale backend). While the timeout itself might be due to network conditions, the application should not crash.

## Proposed Changes

### [Network Layer]

#### [MODIFY] [RetrofitClient.kt](file:///home/mcsilk/AndroidStudioProjects/Sonus/app/src/main/java/com/example/sonus/network/RetrofitClient.kt)
- Reduce the default timeout from 60 seconds to 30 seconds. 60 seconds is excessively long for a mobile app and contributes to a poor user experience when the network is down.

### [Repository Layer]

#### [MODIFY] [MusicRepository.kt](file:///home/mcsilk/AndroidStudioProjects/Sonus/app/src/main/java/com/example/sonus/repository/MusicRepository.kt)
- Wrap network calls in `getUserPlaylists`, `getFavoriteSongs`, `getLibraryAlbums`, and `enrichSongMetadata` with `try-catch` blocks to prevent exceptions from propagating to the calling ViewModels unhandled.
- Ensure that in case of a network error, the repository returns cached data or an empty list instead of throwing.

### [UI Layer]

#### [MODIFY] [HomeViewModel.kt](file:///home/mcsilk/AndroidStudioProjects/Sonus/app/src/main/java/com/example/sonus/ui/home/HomeViewModel.kt)
- Add a `try-catch` block inside `loadRecentlyPlayed` to gracefully handle any errors during metadata enrichment.

#### [MODIFY] [LibraryViewModel.kt](file:///home/mcsilk/AndroidStudioProjects/Sonus/app/src/main/java/com/example/sonus/ui/library/LibraryViewModel.kt)
- Add `try-catch` blocks inside `fetchLibraryData` and `toggleFavorite` to handle network timeouts and other IO exceptions.

## Verification Plan

### Automated Tests
- N/A (Manual verification is more suitable for network timeout simulation).

### Manual Verification
- Simulate a network timeout (e.g., by changing the `BASE_URL` to an unreachable IP or turning off the server/VPN).
- Verify that the app no longer crashes when navigating to the Home or Library screens.
- Verify that the app shows cached data or an empty state instead of crashing.
