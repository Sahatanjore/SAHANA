# SAHANA v2 build fix

The GitHub Actions log showed the Android module was discovered correctly, but Kotlin compilation failed because:
- androidx.fragment was missing while MainActivity extends FragmentActivity.
- androidx.biometric was missing while App Lock uses BiometricManager/BiometricPrompt.
- the notification referenced a missing ic_veera_logo resource.

v2 adds the required Fragment and Biometric dependencies and provides a SAHANA notification icon. Remaining Material experimental API messages are warnings, not the build failure.
