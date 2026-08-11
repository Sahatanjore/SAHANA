# SAHANA v3 build fixes

Based on the latest GitHub Actions log:

- Opted EditEntryDialog and AddEntryDialog into ExperimentalMaterial3Api because ExposedDropdownMenuBox/menuAnchor APIs are experimental in the pinned Material3 version.
- Replaced unresolved ComponentActivity check with FragmentActivity, matching MainActivity's base class.
- Replaced missing ic_veera_logo with a valid SAHANA notification icon resource.
- Removed an accidental duplicate local `toAccount` declaration.
- Kept the existing AndroidX Fragment and Biometric dependencies.
- No third-party character/theme artwork was added.
