# Poprawa działania aplikacji przy słabym połączeniu internetowym (Podejście Offline-First)

Implementacja kompleksowego rozwiązania opartego na architekturze offline-first, optymalizacji sieciowej oraz inteligentnym buforowaniu.

## User Review Required

> [!IMPORTANT]
> Przechodzimy z `SharedPreferences` na bazę danych `Room`. Wymaga to dodania nowych zależności w projekcie. Stary cache zostanie nadpisany nowym systemem bazy danych przy pierwszym uruchomieniu.

> [!NOTE]
> Wprowadzamy `WorkManager` do obsługi operacji w tle, co pozwoli na synchronizację polubień utworów nawet po zamknięciu aplikacji lub powrocie do zasięgu sieci.

## Proposed Changes

### Konfiguracja Projektu i Zależności

Dodanie Room, WorkManager oraz optymalizacja wersji.

#### [MODIFY] [libs.versions.toml](file:///home/mcsilk/AndroidStudioProjects/Sonus/gradle/libs.versions.toml)
#### [MODIFY] [build.gradle.kts](file:///home/mcsilk/AndroidStudioProjects/Sonus/app/build.gradle.kts)

---

### Warstwa Sieciowa i Buforowanie Obrazów

Optymalizacja `Retrofit` oraz `Glide`.

#### [MODIFY] [RetrofitClient.kt](file:///home/mcsilk/AndroidStudioProjects/Sonus/app/src/main/java/com/example/sonus/network/RetrofitClient.kt)
- Zwiększenie timeoutów do 30s.
- Dodanie mechanizmu `OkHttp Cache` (10MB).
- Implementacja prostego interceptora ponawiającego (Retry).

#### [MODIFY] [GlideHelper.kt](file:///home/mcsilk/AndroidStudioProjects/Sonus/app/src/main/java/com/example/sonus/network/GlideHelper.kt)
- Konfiguracja `DiskCacheStrategy.ALL` dla agresywnego buforowania okładek.

---

### Baza Danych (Room) [NEW]

Zastąpienie `LibraryCacheManager` profesjonalnym systemem bazodanowym.

#### [NEW] Entities (SongEntity, AlbumEntity, PlaylistEntity)
#### [NEW] DAOs (MusicDao)
#### [NEW] SonusDatabase

---

### Repozytorium i Offline-First Logic

Aktualizacja logiki pobierania danych.

#### [MODIFY] [MusicRepository.kt](file:///home/mcsilk/AndroidStudioProjects/Sonus/app/src/main/java/com/example/sonus/repository/MusicRepository.kt)
- Logika: Zawsze zwracaj dane z Room natychmiast, a w tle odświeżaj je z sieci (Single Source of Truth).
- Obsługa błędów sieci bez przerywania działania UI.

---

### Synchronizacja w tle i Optimistic UI

#### [NEW] SyncWorker
- Klasa odpowiedzialna za wysyłanie zakolejkowanych polubień (Favorites) gdy sieć wróci.

#### [MODIFY] [LibraryViewModel.kt](file:///home/mcsilk/AndroidStudioProjects/Sonus/app/src/main/java/com/example/sonus/ui/library/LibraryViewModel.kt)
- Implementacja "Optimistic UI" dla polubień – zmiana stanu w bazie danych i UI natychmiast, kolejkowanie zadania w `WorkManager`.

## Verification Plan

### Automated Tests
- Będę weryfikował poprawność budowania projektu po dodaniu zależności.

### Manual Verification
1.  **Test trybu samolotowego:** Sprawdzenie czy biblioteka ładuje się natychmiast z bazy Room.
2.  **Test słabego łącza:** Sprawdzenie czy zwiększone timeouty zapobiegają błędom.
3.  **Test synchronizacji:** Polubienie utworu offline -> wyłączenie trybu samolotowego -> sprawdzenie czy `WorkManager` zsynchronizował zmianę z serwerem.
