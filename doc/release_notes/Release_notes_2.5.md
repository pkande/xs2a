# Release notes v.2.5

## Deleted deprecated method createConsent in CmsAspspPiisService
Method `de.adorsys.psd2.consent.aspsp.api.piis.CmsAspspPiisService#createConsent(PsuIdData, TppInfo, List, LocalDate, int)` was removed,
use `CmsAspspPiisService#createConsent(PsuIdData, CreatePiisConsentRequest)` instead.

## New mechanism for counting frequencyPerDay
From now on, we count the number of consent usages by every endpoint:

- /accounts
- /accounts/account-id per account-id
- /accounts/account-id/transactions per account-id
- /accounts/account-id/balances per account-id
- /accounts/account-id/transactions/transaction-id per account-id and transaction-id, if applicable.

If the amount of accesses for any of these endpoint is exceeded - the `429 ACCESS_EXCEEDED` is returned. All other
endpoints are still accessible until their amount is not exceeded.

Also, the `usageCounter` field in `AisAccountConsent` is deprecated - now the new field `usageCounterMap` should be used
instead. It is a map: key is the endpoint, value is a number of its usage. Interfaces `CmsAspspAisExportService` and
`CmsPsuAisService` are affected.

