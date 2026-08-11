SAHANA v7 logo build fix

GitHub Actions reported a duplicate Android resource:
drawable/ic_sahana_logo.png and drawable/ic_sahana_logo.xml had the same resource name.

v7 keeps the actual SAHANA PNG logo and removes the old duplicate XML resource.
The manifest continues to use the SAHANA logo for icon and roundIcon.
