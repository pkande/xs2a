# Release notes v.2.5

## Deleted deprecated method createConsent in CmsAspspPiisService
Method `de.adorsys.psd2.consent.aspsp.api.piis.CmsAspspPiisService#createConsent(PsuIdData, TppInfo, List, LocalDate, int)` was removed,
use `CmsAspspPiisService#createConsent(PsuIdData, CreatePiisConsentRequest)` instead.

## Bugfix: Wrong error code "requestedExecutionDate" value in the past
Error code was changed to `400 EXECUTION_DATE_INVALID` from `400 FORMAT_ERROR` when `requestedExecutionDate` field is less then current date.
