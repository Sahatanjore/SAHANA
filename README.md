# AINA

Premium personal finance app foundation.

## Included in v1 foundation
- Income / expense tracking
- Balance and savings calculations
- Goals
- Insights
- Backup / restore
- App lock support from the existing finance source
- Clean Android/Kotlin project structure
- Camera FileProvider foundation
- GitHub Actions release APK build

## Build
`gradle :app:assembleRelease --no-daemon --stacktrace`

The project intentionally keeps AI/web access out of the APK until a secure backend/API configuration is provided. Never hard-code an AI API key into the application.
