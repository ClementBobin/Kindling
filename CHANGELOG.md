# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

- Initial multi-module Kotlin library scaffold.
- `KindlingShapes.borderWidth` (default `1.dp`): one theme-level token for the border thickness of every core component, next to `base` for the corner radius. `0.dp` turns borders off.
- `KindlingShapes.borderWidthThin` (0.5×), `borderWidthThick` (2×) and `borderStroke(color, width)`, plus the `Modifier.kindlingBorder(width, color, shape)` helper, which draws nothing for a zero width instead of a hairline.
- `:android` Firebase multi-tenant helpers (`dev.kindling.android.firebase`): one app can use a different Firebase project per client by initialising `FirebaseApp` at runtime instead of embedding a single `google-services.json`. Fill the `KFirebaseConfig` DTO from your own API or file handling and pass it to `KFirebaseManager`, which initialises/restores the active tenant and fetches its FCM token; `KSharedPrefsFirebaseConfigStore` persists the DTO so it can be restored in `Application.onCreate()`.
- `:android` now depends on `firebase-messaging` (exposed as `api`), and its unit tests run on JUnit 5.

### Changed

- Core components no longer hardcode border thickness; they read it from `KindlingShapes`. This covers KButton, KBadge, KInput, KTextarea, InputGroup, InputOTP, KCombobox, KDataTable, KDropzone, KFileItem, KEmpty, KAvatar, KStepper, popovers, dialogs and card footers, the consent and questionnaire components, and the roulette picker. At the defaults they render as before.
- `KInput` and `KTextarea` now use the theme radius for the field container (it was a fixed 8.dp inside a theme-radius clip).
- The Create page in the docs site has a border-width control, and the generated theme code includes `borderWidth`.
