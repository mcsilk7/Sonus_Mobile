# 🔌 Dokumentacja API SONUS

Ten plik zawiera zestawienie wszystkich punktów końcowych (endpoints) oraz struktur danych (DTO), z których korzysta aplikacja Sonus.

---

## 🔐 Autentykacja (AuthApi)

| Metoda | Endpoint | Opis | Body |
| :--- | :--- | :--- | :--- |
| `POST` | `authenticate` | Logowanie użytkownika | `LoginRequest` |
| `POST` | `api/user/register` | Rejestracja nowego konta | `RegisterRequest` |

---

## 🎵 Utwory i Wyszukiwanie (SearchApi)

| Metoda | Endpoint | Opis | Query Params |
| :--- | :--- | :--- | :--- |
| `GET` | `api/songs/search` | Wyszukiwanie utworów | `q` (String) |
| `GET` | `api/albums/search` | Wyszukiwanie albumów | `title` (String) |
| `GET` | `api/songs/{id}/stream` | Strumieniowanie audio | - |
| `GET` | `api/songs/{id}/cover` | Pobieranie okładki utworu | - |

---

## 📂 Playlisty (PlaylistApi)

| Metoda | Endpoint | Opis |
| :--- | :--- | :--- |
| `GET` | `api/playlists` | Wszystkie publiczne playlisty |
| `GET` | `api/playlists/user/{userId}` | Playlisty konkretnego użytkownika |
| `GET` | `api/playlists/{id}` | Szczegóły playlisty |
| `GET` | `api/playlists/{id}/songs` | Lista utworów w playliście |
| `GET` | `api/playlists/{id}/songs/count` | Liczba utworów w playliście |
| `POST` | `api/playlists/user/{userId}` | Tworzenie nowej playlisty |
| `POST` | `api/playlists/{pid}/songs/{sid}`| Dodawanie utworu do playlisty |
| `DELETE` | `api/playlists/{pid}/songs/{sid}`| Usuwanie utworu z playlisty |
| `DELETE` | `api/playlists/{id}` | Usuwanie całej playlisty |

---

## 💿 Albumy (AlbumApi)

| Metoda | Endpoint | Opis |
| :--- | :--- | :--- |
| `GET` | `api/albums/library/user/{userId}`| Albumy zapisane w bibliotece |
| `GET` | `api/albums/{id}` | Szczegóły albumu |
| `GET` | `api/albums/{id}/songs` | Utwory w albumie |
| `POST` | `api/albums/{aid}/library/{uid}` | Dodawanie albumu do biblioteki |
| `DELETE` | `api/albums/{aid}/library/{uid}` | Usuwanie albumu z biblioteki |

---

## ❤️ Ulubione (FavoriteApi)

| Metoda | Endpoint | Opis |
| :--- | :--- | :--- |
| `GET` | `api/favorites/{userId}` | Lista ulubionych utworów |
| `POST` | `api/favorites/{uid}/songs/{sid}/toggle`| Przełączanie statusu ulubionego |
| `GET` | `api/favorites/{uid}/songs/{sid}/check` | Sprawdzenie czy utwór jest ulubiony |

---

## 📦 Modele Danych (DTOs)

### `SongDTO`
```kotlin
data class SongDTO(
    val id: Long,
    val title: String,
    val artist: String,
    val duration: Int?,
    val coverPath: String?,
    val filePath: String?,
    val albumId: Long?,
    var isFavorite: Boolean,
    var isInPlaylist: Boolean
)
```

### `PlaylistDTO`
```kotlin
data class PlaylistDTO(
    val id: Long?,
    val name: String,
    val description: String?,
    val songs: List<SongDTO>?,
    val songCount: Int?
)
```

### `AlbumDTO`
```kotlin
data class AlbumDTO(
    val id: Long?,
    val title: String,
    val artist: String,
    val coverPath: String?,
    val songIds: List<Long>?,
    val songs: List<SongDTO>?,
    var isSaved: Boolean
)
```

### `AuthResponse`
```kotlin
data class AuthResponse(
    val token: String,
    val username: String,
    val userId: Long,
    val role: String?
)
```
