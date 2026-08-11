SAHANA v12 UI language compile fix

The GitHub build failed because SahaHomeCard() received an AppLanguage parameter
named `language`, but its two translation calls referenced `uiLanguage`, which
is scoped to HomeScreen and therefore unavailable inside SahaHomeCard.

Fixed:
- SahaHomeCard line 573: tr(language, ...)
- SahaHomeCard line 574: tr(language, ...)

The HomeScreen call already passes `language` to SahaHomeCard.
No other feature code was changed for this fix.
