# Omerta

> *A code of silence. A game of shadows.*

Omerta is a companion app for living-room **Mafia** / **Werewolf**-style social-deduction games. It runs the entire session on the host's phone — role assignment, the cinematic role reveal, phase timers, eliminations, win conditions — so the table can focus on accusations and bluffing instead of paperwork.

Two play modes:

- **Local Mode** — one phone passed around the table. Each player taps to reveal their role, then hands it on. Ideal for 3–10 players in a room.
- **LAN Mode** — one phone per player on the same Wi-Fi. The host's device runs the authoritative game; everyone else joins by code or QR. Ideal for 5–16 players.

No internet required, no accounts, no ads, no analytics.

---

## Highlights

- **11 built-in roles** — Citizen, Mafia, Sheriff, Doctor, Don, Maniac, Detective, Bodyguard, Courtesan, Mayor, and a **Custom Role editor** for authoring your own (with localized name + description fields, team alignment, and an icon picker).
- **Cinematic Role Reveal** — face-down ornate card with breathing animation → tap → 3-second suspense → Y-axis flip → particle burst tinted by team color → settle on a team-tinted card with the role's icon, name, alignment badge, and ability description.
- **Animated Phase Tracker** — distinct atmospheric backgrounds for Night (drifting cool particles + moon), Day (warm gradient + sun rays), and Voting (deep red + voting particles). Circular Canvas-drawn countdown timer that turns critical-red below 10 seconds. Host controls panel for skip / pause / +30s / +60s / end-game.
- **Interactive How to Play** — 9-panel cinematic rules storyboard + 7-step phone-in-phone walkthrough of the app flow.
- **Instant localization** — three languages (English, Russian, Uzbek) with a runtime language switcher that re-localizes every screen without restarting the app or popping the navigation stack.
- **Custom visual language** — all icons hand-built as Compose `ImageVector` paths (no `Icons.Default.*`), custom fonts (Cinzel for display, Inter for body, JetBrains Mono for room codes) loaded via Google Fonts, glass-morphism cards, multi-layer atmospheric backgrounds, breathing logo glow on Home.
- **Custom transitions** — push/pop nav transitions that rise-in-and-scale rather than slide. Cards stagger-enter on Home with bounce-in spring physics.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin 2.0.21 |
| UI | Jetpack Compose (BOM 2024.12.01) — Material 3, Foundation, Animation, Navigation |
| Build | AGP 8.7.3, Gradle 8.9, JDK 17+ |
| State | Compose `MutableStateFlow` + ViewModel + `collectAsStateWithLifecycle` |
| Persistence | DataStore Preferences (settings, presets) |
| Serialization | `kotlinx.serialization` JSON (preset blobs, network protocol) |
| Networking (LAN) | Ktor 2.3.13 WebSocket server + client over `_omerta._tcp` advertised via Android NSD |
| QR | ZXing 3.5.3 (generation), ML Kit barcode-scanning 17.3.0 + CameraX 1.4.1 (scanning) |
| Permissions | Accompanist Permissions 0.36.0 |
| Min / Target / Compile SDK | 26 / 34 / 35 |

---

## Architecture

```
app/src/main/java/uz/angrykitten/omerta/
├── OmertaApp.kt                       Application + tiny service locator
├── MainActivity.kt                    ComponentActivity → setContent → OmertaNavHost
│
├── domain/
│   ├── model/                         Plain @Serializable data classes (Role, Player,
│   │                                  Room, Preset, Team, GamePhase, GameMode,
│   │                                  AppLanguage, RoleLocalization)
│   ├── catalog/DefaultRoles.kt        Built-in role catalog with localized strings baked in
│   └── game/
│       ├── RoomCode.kt                6-char alphanumeric, ambiguous glyphs excluded
│       ├── RoomConfig.kt              Host's setup choices (roles/timers/options)
│       └── GameSession.kt             Authoritative game state (StateFlow-driven)
│
├── data/
│   ├── settings/SettingsRepository.kt DataStore-backed language + theme prefs
│   ├── presets/PresetRepository.kt    DataStore-backed JSON preset CRUD
│   └── network/                       LAN: NSD + Ktor server + Ktor client + sealed
│                                      NetworkMessage protocol with reconnect
│
└── ui/
    ├── theme/                         Color tokens (dark + light), typography
    │                                  (Cinzel/Inter/JetBrains Mono via Google Fonts),
    │                                  spacing scale, theme engine, gradient helpers
    ├── icons/                         Custom-drawn ImageVector icons (UI + role)
    ├── locale/LocaleProvider.kt       LocalAppLanguage + Context-wrapper that swaps
    │                                  resources while keeping the Activity reachable
    │                                  via baseContext (so Accompanist / Coil etc. work)
    ├── navigation/                    Destination routes + NavHost + cinematic
    │                                  push/pop transitions
    ├── common/                        GlassCard, DepthBackground, ChapterMarker,
    │                                  OmertaTopBar, PrimaryButton, SecondaryButton,
    │                                  QrCodeView (ZXing-on-Canvas)
    ├── home/                          HomeScreen, ParticleField, ActionCard
    ├── settings/                      SettingsScreen + SettingsViewModel
    ├── howtoplay/                     HowToPlayScreen, StoryboardPanels (9),
    │                                  WalkthroughSteps (7)
    ├── createroom/                    ModeSelection, RuleSetup, RoleEditor, Lobby
    ├── joinroom/                      JoinRoomScreen (tabbed: code entry + QR scan)
    │                                  + QrScannerView (CameraX + ML Kit)
    └── game/                          RoleRevealScreen, PhaseTrackerScreen, EndGameScreen
```

### State flow

`GameSession` is a process-wide singleton held by `OmertaApp`. Both Local and LAN modes read/write through it. Screens observe via `StateFlow.collectAsStateWithLifecycle()` so there's no prop-drilling through nav arguments.

### LAN protocol

Sealed `NetworkMessage` class with `kotlinx.serialization` polymorphic JSON. Host runs a Ktor WebSocket server on an OS-picked free port, advertised via Android's `NsdManager` under service type `_omerta._tcp`. Clients discover via `discoverServices()`, connect to `ws://<host>:<port>/omerta`, and reconnect with exponential backoff (500 ms → 1 s → 2 s → 4 s → 8 s, max 5 attempts) on disconnect.

### Theme engine

`OmertaTheme` provides both a Material3 `ColorScheme` and a custom `OmertaColors` `CompositionLocal` (team colors, glow, success, divider). Two palettes: **noir** (dark, near-black `#0A0A0F` background with crimson + gold accents) and **parchment** (light, warm `#F5ECD7` with dark-crimson + antique-gold accents). Theme tri-state: `SYSTEM` / `DARK` / `LIGHT` persisted in DataStore.

### Locale switching without restart

`ProvideAppLanguage` overrides `LocalContext`, `LocalConfiguration`, and `LocalAppLanguage` together. The overridden context is a `LocaleAwareContextWrapper` whose `getResources()` returns locale-modified resources but whose `baseContext` is still the Activity — so libraries that walk the base-context chain (Accompanist, Coil, system services) keep working. Every `stringResource()` lookup recomposes against the new strings.xml in the same frame.

---

## Build & Run

### Prerequisites

- JDK 17 or 19 (the project's `gradle/gradle-daemon-jvm.properties` requires `toolchainVersion=17`)
- Android SDK with platform-tools, build-tools 34+, and platform 35
- A device or emulator running Android 8.0 (API 26) or higher

### From Android Studio

Open the project in Android Studio Hedgehog (2023.1) or newer. Let it sync, then hit **Run**.

### From the command line

```bash
# Set JAVA_HOME to a JDK ≥17 if the daemon can't find one
export JAVA_HOME=/path/to/jdk-17  # or 19, 21, etc.

# Debug build (development)
./gradlew assembleDebug
./gradlew installDebug

# Release build (signed with debug key per the current buildTypes config)
./gradlew assembleRelease
# APK lands at: app/build/outputs/apk/release/app-release.apk

# Run unit tests
./gradlew testDebugUnitTest
```

### Adb logs

The QR scanner logs under tag `OmertaScanner`:

```bash
adb logcat -s OmertaScanner CameraX
```

---

## Localization

Three locales: `values/` (English, default), `values-ru/` (Russian), `values-uz/` (Uzbek — Latin script). Every string key exists in all three files; role names and descriptions in `DefaultRoles.kt` carry their own per-language fields (so they travel correctly over the LAN protocol without depending on the client's `strings.xml` being in sync).

To add a new language:

1. Add an entry to the `AppLanguage` enum in `domain/model/AppLanguage.kt`.
2. Create `app/src/main/res/values-<tag>/strings.xml` with the full key set.
3. Add the locale tag to `app/src/main/res/xml/locales_config.xml`.
4. Add a `LanguageRow(...)` call in `SettingsScreen.LanguageSection`.
5. Add a `name<Lang>` field for every built-in role in `DefaultRoles.kt` and update `RoleLocalization.kt` to read it.

---

## Project Status / Honest Scope Notes

What's fully wired and runnable end-to-end:

- Home → Settings (language + theme switchers apply instantly)
- Home → How to Play (storyboard + walkthrough)
- Home → Create Room → Mode select → Rule Setup → Custom Role Editor → Lobby → cinematic Role Reveal → animated Phase Tracker → End Game → Play Again
- QR code generation in the host's Lobby, QR scanning in the guest's Join Room (inline, no separate route)
- Theme + language preferences persisted across launches
- Full localization audited across all three languages

Built but not yet wired through the UI flow (next on the roadmap):

- **LAN end-to-end** — the networking layer (NSD register/discover, Ktor server/client, sealed `NetworkMessage` protocol, reconnect logic) is fully implemented in `data/network/`, but the host-side ViewModel that pumps `GameSession` changes onto the wire (broadcasting `GameStarted`, `PhaseChanged`, `TimerUpdate`, `PlayerEliminated`, `GameEnded`) and the client-side ViewModel that mirrors host state back into the local `GameSession` aren't yet attached. Local mode works fully on a single device; LAN screens currently route back to Home after code/QR entry instead of opening a Waiting Room.
- **Preset loading in UI** — `PresetRepository` does full CRUD with JSON-on-DataStore persistence. The "Load Preset" row isn't yet rendered in `RuleSetupScreen`.

Neither gap requires changing anything already in place.

---

## Credits

Built as a code-craft demonstration with these design references in mind:
- Mafia / Werewolf parlor-game tradition for the rules
- Noir film typography for the Cinzel + tracking-wide caps look
- Editorial / cinema-credit design for the `00 // SECTION` chapter markers

Custom icons hand-built using `ImageVector` paths. Fonts loaded via Google Fonts (`compose-ui-text-google-fonts`).

---

## License

This codebase is a personal project. No license has been chosen yet — treat it as **all rights reserved** until a `LICENSE` file is added. If you'd like to use any of the code, please open an issue first.
