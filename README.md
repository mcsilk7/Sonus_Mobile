# 🎙️ SONUS Studio System
### *Modern Music Player with 80s Industrial Aesthetic*

**Sonus** to zaawansowany system odtwarzania i zarządzania sygnałami dźwiękowymi na system Android. Aplikacja łączy nowoczesną architekturę (Single Activity, Jetpack Navigation) z surowym, technicznym stylem wizualnym wzorowanym na profesjonalnych konsolach studyjnych i magnetofonach szpulowych z lat 80.

---

## 🛠️ Architektura i Technologie

Projekt został zbudowany zgodnie z najnowszymi standardami programowania na platformę Android:

*   **Język:** Kotlin (100%)
*   **Architektura:** MVVM (Model-View-ViewModel) + Repository Pattern.
*   **UI:** Single Activity Architecture z wykorzystaniem **Jetpack Navigation Component**.
*   **Sieć:** Retrofit 2 + OkHttp (obsługa sesji, tokenów JWT oraz przechwytywanie błędów 401).
*   **Wielowątkowość:** Kotlin Coroutines (płynne, równoległe pobieranie danych).
*   **Media:** MediaPlayer + Foreground Service (odtwarzanie w tle z powiadomieniami).
*   **Wizualizacje:** Custom Drawings (Canvas API) dla symulacji analogowych mechanizmów.

---

## 📡 API i Dane
Szczegółowa lista wszystkich punktów końcowych oraz modeli danych znajduje się w osobnym pliku:
👉 **[DOKUMENTACJA API (API_DOCS.md)](API_DOCS.md)**

---

## 📼 Kluczowe Funkcje

### 1. Panel Sterowania (DIR / Home)
*   **Modularne karty:** Szybki dostęp do modułów `FAV_DATA` (Ulubione) oraz `PL_DATA` (Playlisty).
*   **Signal History Log:** Inteligentna lista ostatnio odtwarzanych kanałów (utworów).
*   **Dynamiczny Avatar:** Automatyczne generowanie litery operatora na podstawie zalogowanego profilu.

### 2. Monitor Sygnału (Player)
*   **Retro Reels:** Realistyczna symulacja magnetofonu szpulowego.
    *   *Tape Transfer Physics:* Wizualny przepływ taśmy z szpuli podawczej na odbiorczą w miarę postępu utworu.
    *   *Smooth Rotation:* Animacja oparta na czasie rzeczywistym, eliminująca szarpnięcia.
*   **Noise Overlay:** Subtelny efekt ziarna i linii skanowania CRT, nadający ekranowi analogowy charakter.

### 3. Zarządzanie Kolejką (Station Queue)
*   **Drag & Drop:** Możliwość zmiany kolejności utworów "w locie" poprzez przeciąganie pozycji na liście.
*   **Gesty Swipe:** 
    *   *Swipe Right:* Natychmiastowe dodanie sygnału do kolejki (`Add to Queue`).
    *   *Swipe Left:* Szybkie polubienie/odlubienie utworu.

### 4. Archiwum Sygnałów (Library)
*   **Modularne zarządzanie:** Podział na sekcje `STORAGE_PLAYLISTS` oraz `STORAGE_ALBUMS`.
*   **Initialize Playlist:** Intuicyjny proces tworzenia nowych jednostek pamięci (playlist) z walidacją danych.

---

## 🎨 Design: Retro Studio Theme

Aplikacja implementuje unikalny system wizualny oparty na palecie **#RetroStudio**:
*   **Background:** `#14120F` (Głęboka czerń studyjna).
*   **Accent:** `#E8A13A` (Bursztynowy kolor retro-wyświetlaczy).
*   **Status Red:** `#C0544A` (Ostrzegawcze diody systemowe).
*   **Typography:** Monospace (Styl terminalowy).
*   **Wykończenie:** Promień zaokrąglenia 6dp, industrialne obramowania modułów.

---

## 📂 Struktura Projektu

```text
com.example.sonus
├── network/          # API, DTOs, AuthInterceptor, SessionManager
├── repository/       # MusicRepository (Źródło prawdy dla danych)
├── ui/               # Podział na moduły UI
│   ├── auth/         # Login, Register
│   ├── home/         # DIR (Ekran główny)
│   ├── search/       # SRCH (Wyszukiwarka)
│   ├── library/      # LIB / FAVORITES (Biblioteka i Ulubione)
│   ├── player/       # Monitoring i odtwarzanie (Custom Views: Reels, Noise)
│   └── profile/      # USER_ID_CONTROL (Ustawienia operatora)
├── PlayerState       # Singleton zarządzający stanem odtwarzacza
└── PlaybackService   # Serwis działający w tle (Media Playback)
```

---

## 🚀 Instalacja i Uruchomienie

1.  Sklonuj repozytorium.
2.  Otwórz projekt w **Android Studio Jellyfish (lub nowszym)**.
3.  Zsynchronizuj pliki Gradle (wymagane Gradle 8.0+).
4.  Upewnij się, że Twój serwer backendowy jest dostępny pod adresem zdefiniowanym w `RetrofitClient`.
5.  Uruchom aplikację na urządzeniu lub emulatorze (min. API 24).

---

## 📝 Uwagi do wersji 2.0
W wersji 2.0 system przeszedł pełną migrację na **Single Activity**, co wyeliminowało przeskoki audio podczas zmiany ekranów i pozwoliło na zaimplementowanie płynnych przejść **ViewPager2** (Swipe między głównymi panelami).

---
*Created by Operator SONUS v1.0.0*
