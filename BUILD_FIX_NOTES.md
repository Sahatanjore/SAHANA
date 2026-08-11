# AINA v3 build fixes

Based on the latest GitHub Actions log:

- Opted EditEntryDialog and AddEntryDialog into ExperimentalMaterial3Api because ExposedDropdownMenuBox/menuAnchor APIs are experimental in the pinned Material3 version.
- Replaced unresolved ComponentActivity check with FragmentActivity, matching MainActivity's base class.
- Replaced missing ic_veera_logo with a valid AINA notification icon resource.
- Removed an accidental duplicate local `toAccount` declaration.
- Kept the existing AndroidX Fragment and Biometric dependencies.
- No third-party character/theme artwork was added.


## v4 fix

The new Actions log reaches Kotlin code generation but fails inside `remember()` with `Couldn't inline method call` / `couldn't find inline method ... androidx.compose.runtime.ComposablesKt.remember`. The project uses Kotlin 2.0.21 with Jetpack Compose, but the Kotlin Compose compiler plugin was not applied. v4 applies `org.jetbrains.kotlin.plugin.compose` version 2.0.21 at both root and app levels.
