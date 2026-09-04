# 🎙️ SONUS Mobile Studio
### *Advanced Android Music Player with 80s Industrial Aesthetic*

**Sonus** to zaawansowana aplikacja mobilna do odtwarzania i zarządzania sygnałami dźwiękowymi na systemie Android. Aplikacja łączy nowoczesną technologię strumieniowania z surowym, technicznym stylem wizualnym wzorowanym na profesjonalnych konsolach studyjnych i magnetofonach szpulowych z lat 80. Zapewnia użytkownikom pełną kontrolę nad ich biblioteką muzyczną, oferując bezpieczne połączenia, zaawansowaną organizację utworów oraz unikalne wrażenia audiowizualne.

Cała kluczowa logika biznesowa, system odtwarzania w tle oraz integracja z protokołami bezpieczeństwa są zaimplementowane bezpośrednio w module mobilnym, zapewniając najwyższą wydajność i natywne doświadczenie użytkownika.

---

## 🛠️ Architektura i Technologie (Android)

Aplikacja została zbudowana w oparciu o nowoczesny stos technologiczny Android:

*   **Język:** Kotlin (100%)
*   **UI Architecture:** Single Activity Architecture + Jetpack Navigation Component.
*   **Interfejs:** Klasyczne Widoki (XML) z zaawansowanymi komponentami customowymi (Custom Views).
*   **Baza Danych:** Room Persistence Library (lokalna baza danych sygnałów i metadanych).
*   **Sieć:** Retrofit 2 + OkHttp (komunikacja z API, obsługa sesji i Auth).
*   **Odtwarzanie:** Android Media Session + Service (stabilne odtwarzanie w tle).
*   **Bezpieczeństwo:** WireGuard Android Tunnel SDK (zintegrowany VPN).
*   **Wielowątkowość:** Kotlin Coroutines & Flow.
*   **Zarządzanie Danymi:** Paging 3 (płynne przewijanie dużych bibliotek).

---

## 📼 Kluczowe Funkcje Mobilne

### 1. Panel Sterowania (Home)
*   **Modularne karty:** Błyskawiczny dostęp do ulubionych sygnałów (`FAV_DATA`) i list odtwarzania (`PL_DATA`).
*   **Signal History Log:** Historia ostatnio odtwarzanych kanałów w formacie terminalowym.
*   **Terminal Log:** Wizualny podgląd operacji systemowych w czasie rzeczywistym.
*   **Status Sesji:** Dynamiczne wyświetlanie roli użytkownika (`OPERATOR_ID`) i stanu połączenia.

### 2. Monitor Sygnału (Player)
*   **Retro Reels:** Interaktywna symulacja magnetofonu szpulowego z fizyką obrotu.
*   **Tape Level Progress:** Wizualizacja ilości taśmy na szpulach zależna od postępu utworu.
*   **Noise Overlay:** Efekty ziarna CRT i linii skanowania dla autentycznego klimatu retro.
*   **Gestures:** Obsługa gestu *Swipe Down* (przesunięcie w dół) w celu zamknięcia panelu monitora.
*   **Queue Management:** Dynamiczna lista kolejki (Bottom Sheet) z możliwością podglądu nadchodzących sygnałów.
*   **Playback Service:** Niezawodne odtwarzanie w tle z pełną kontrolą z poziomu ekranu blokady i systemowych powiadomień.

### 3. Zarządzanie Playlistami (Playlist Protocol)
*   **Personalizowane Archiwa:** Tworzenie i edytowanie własnych list odtwarzania (inicjalizacja jednostek dyskowych).
*   **Integracja Jednostek:** Dodawanie piosenek do wielu playlist jednocześnie (ikona "+").
*   **Modyfikacja Struktury:** Usuwanie pojedynczych utworów (akcja `WIPE`) oraz pełne kasowanie playlist.
*   **Sortowanie Logiczne:** Filtrowanie zawartości według Tytułu, Artysty, Czasu trwania lub kolejności domyślnej.
*   **Metadata Enrichment:** Automatyczne uzupełnianie metadanych utworów przy dodawaniu do list.

### 4. Skany i Wyszukiwanie (Scanner Unit)
*   **Debounced Search:** Błyskawiczne przeszukiwanie bazy danych z inteligentnym opóźnieniem (oszczędność transferu).
*   **Search History:** Pamięć ostatnio wyszukiwanych fraz z opcją selektywnego usuwania wpisów z historii.
*   **Dual Results:** Jednoczesne wyszukiwanie w kategoriach Utwory oraz Albumy.
*   **Offline Warning:** Powiadomienie systemowe przy próbie skanowania w trybie braku łączności.

### 5. Archiwum i Offline (Library & Downloads)
*   **Download Manager:** Pobieranie sygnałów do pamięci lokalnej w formacie MP3 wraz z metadanymi JSON.
*   **Offline Protocol:** Pełna funkcjonalność biblioteki bez sieci – system automatycznie wykrywa i odtwarza lokalne kopie.
*   **Storage Monitor:** Wizualny wskaźnik zajętości pamięci (*Sector Map*) oraz licznik MB wykorzystanych na urządzeniu.
*   **Batch Management:** Funkcja `EXE_WIPE` pozwalająca na masowe usunięcie wszystkich pobranych sygnałów jednym kliknięciem.

### 6. Bezpieczne Łącze (VPN / Secure Uplink)
*   **WireGuard Integration:** Wbudowany klient VPN zintegrowany z systemowym Service'em.
*   **Tunnel Monitor:** Stałe monitorowanie stanu tunelu i automatyczne wznawianie bezpiecznego połączenia.
*   **Auth Interceptor:** Zabezpieczona komunikacja z API przy użyciu rotujących tokenów sesji.

### 7. Personalizacja i System (UI Engine)
*   **Dual-Theme Mode:** Przełączanie między trybem **Industrial** (pełna terminologia techniczna, hex-kody) a **Standard** (klasyczny interfejs).
*   **Visual Toggle:** Możliwość wyłączenia animacji szpul w celu oszczędzania energii (*Mechanical Visuals Toggle*).
*   **BlurHash Placeholders:** Płynne ładowanie okładek z generowanymi w locie rozmazanymi podglądami.
*   **GitHub Auto-Update:** Wbudowany system sprawdzania dostępności nowych wersji APK bezpośrednio z repozytorium.
*   **Monospace Typography:** Zastosowanie czcionek o stałej szerokości dla wzmocnienia efektu terminala komputerowego.

---

## 🚀 Instalacja i Uruchomienie

### Wymagania
*   **Android Studio Jellyfish** (lub nowsze)
*   **Android Device / Emulator:** Minimum API 24 (Android 7.0)
*   **JDK 17**

### Kroki
1.  Sklonuj repozytorium.
2.  Otwórz projekt w Android Studio.
3.  Skonfiguruj klucze VPN w `local.properties` (opcjonalnie dla pełnej funkcjonalności Secure Uplink).
4.  Wybierz moduł `app` i uruchom aplikację.

---

## 🎨 Design: Retro Studio Theme

Aplikacja implementuje unikalny system wizualny:
*   **Background:** `#14120F` (Głęboka czerń studyjna).
*   **Accent:** `#E8A13A` (Bursztynowy kolor retro-wyświetlaczy).
*   **Typography:** Monospace (Styl terminalowy).

---
*Created by Operator SONUS v2.2.1 (Mobile Edition)*
