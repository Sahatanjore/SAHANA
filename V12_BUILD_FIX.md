# SAHANA v12 build fix

Based on GitHub Actions run 31467667728:

1. MainActivity.kt failed because `language` was unresolved in the HomeScreen body.
   v12 reads the persisted AppLanguage locally as `uiLanguage` for the Home labels.
2. GoalReminderReceiver referenced the old `com.veera.expense.R.drawable.ic_veera_logo`.
   v12 now uses `com.sahana.expense.R.drawable.sahana_launcher_logo`.
3. Remaining lowercase legacy `veera_*` storage/channel/report identifiers were
   normalized to `sahana_*`.
4. A workflow preflight rejects legacy package/icon references before Gradle.
5. The existing real SAHANA launcher PNG remains the only current logo resource.

The uploaded log confirms resource merging and signing completed; the failure is
specifically `compileReleaseKotlin` at MainActivity.kt, so the native library
strip message is not the build blocker.
