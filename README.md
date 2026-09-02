# 🎙️ SONUS Studio System
### *Modern Music Player with 80s Industrial Aesthetic*

**Sonus** to zaawansowany system odtwarzania i zarządzania sygnałami dźwiękowymi. Projekt jest budowany w architekturze **Kotlin Multiplatform (KMP)**, co pozwala na uruchamianie aplikacji zarówno na systemie Android, jak i na komputerach stacjonarnych (Desktop).

Aplikacja łączy nowoczesną technologię z surowym, technicznym stylem wizualnym wzorowanym na profesjonalnych konsolach studyjnych i magnetofonach szpulowych z lat 80.

---

## 🛠️ Architektura i Technologie

Projekt wykorzystuje najnowsze standardy programowania wieloplatformowego:

*   **Język:** Kotlin (100%)
*   **Wieloplatformowość:** Kotlin Multiplatform (KMP)
*   **Baza Danych:** Room KMP (współdzielona między Android a Desktop)
*   **Sieć:** Ktor Client (multiplatformowy klient HTTP z obsługą Auth)
*   **UI Android:** Single Activity Architecture + Jetpack Navigation + XML/Views.
*   **UI Desktop:** Compose Multiplatform (deklaratywny interfejs użytkownika).
*   **Wielowątkowość:** Kotlin Coroutines.

---

## 📂 Struktura Projektu

```text
Sonus/
├── shared/           # Kod współdzielony (Logika biznesowa, Modele, DB, API)
│   ├── src/commonMain   # Wspólna logika dla wszystkich platform
│   ├── src/androidMain  # Specyficzna implementacja dla Androida
│   └── src/desktopMain  # Specyficzna implementacja dla Desktop
├── app/              # Moduł aplikacji Android (UI, Playback Service, VPN)
└── desktop/          # Moduł aplikacji Desktop (Compose Multiplatform)
```

---

## 📼 Kluczowe Funkcje

### 1. Panel Sterowania (DIR / Home)
*   **Modularne karty:** Szybki dostęp do modułów `FAV_DATA` (Ulubione) oraz `PL_DATA` (Playlisty).
*   **Signal History Log:** Inteligentna lista ostatnio odtwarzanych kanałów (utworów).

### 2. Monitor Sygnału (Player)
*   **Retro Reels:** Realistyczna symulacja magnetofonu szpulowego (obecnie na Android).
*   **Noise Overlay:** Subtelny efekt ziarna i linii skanowania CRT.

### 3. Archiwum Sygnałów (Library)
*   **Zarządzanie jednostkami pamięci:** Tworzenie i edycja playlist, przeglądanie albumów.
*   **Room KMP:** Pełne wsparcie dla pracy offline na obu platformach.

---

## 🚀 Instalacja i Uruchomienie

### Wymagania
*   **Android Studio Jellyfish** (lub nowsze)
*   **JDK 17** lub nowsze

### Android
1.  Otwórz projekt w Android Studio.
2.  Wybierz konfigurację `app`.
3.  Uruchom na urządzeniu lub emulatorze (min. API 24).

### Desktop (Windows / macOS / Linux)
1.  Wybierz konfigurację `desktop` w Android Studio.
2.  Uruchom aplikację.
3.  Alternatywnie użyj terminala: `./gradlew :desktop:run`

---

## 🎨 Design: Retro Studio Theme

Aplikacja implementuje unikalny system wizualny oparty na palecie **#RetroStudio**:
*   **Background:** `#14120F` (Głęboka czerń studyjna).
*   **Accent:** `#E8A13A` (Bursztynowy kolor retro-wyświetlaczy).
*   **Typography:** Monospace (Styl terminalowy).

---
*Created by Operator SONUS v2.3.0 (KMP Edition)*
