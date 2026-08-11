# SAHANA Premium v1.1

## Implemented foundation
- Separate savings and investment goals
- Home goal overview
- Balance kept separate from goal saved amount
- AI assistant UI and local financial insights
- Multi-theme catalog with 120 named visual identities
- Tamil/English-ready UI
- Sound/vibration settings
- App lock foundation
- Receipt/camera/gallery foundation
- GitHub Actions release build
- Installable test signing

## Important AI note
The APK contains the AI assistant UI and local financial intelligence. Internet/current-information access requires a secure server/Firebase AI integration and an API key supplied by the developer. No secret API key is hard-coded into the APK.

## Theme note
The catalog contains 120 distinct theme identities. A full production theme pack should attach original artwork, animations, sounds and companion assets to each theme; the current catalogue does not pretend that 120 artwork packs were generated when they were not.

## Production signing
The GitHub test build uses the runner debug signing configuration for direct installation. A Play Store build must use a private release keystore stored as a GitHub Actions secret.
