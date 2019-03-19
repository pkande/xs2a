# Release notes v.2.2

## PIIS consent was extended with new properties
The following fields were added to PIIS consent (`de.adorsys.psd2.xs2a.core.piis.PiisConsent`):
| Name                    | Description                                                                                                   |
|-------------------------|---------------------------------------------------------------------------------------------------------------|
| cardNumber              | Card Number of the card issued by the PIISP. Should be delivered if available.                                |
| cardExpiryDate          | Expiry date of the card issued by the PIISP                                                                   |
| cardInformation         | Additional explanation for the card product.                                                                  |
| registrationInformation | Additional information about the registration process for the PSU, e.g. a reference to the TPP / PSU contract |

Also these fields were added to `de.adorsys.psd2.consent.aspsp.api.piis.CreatePiisConsentRequest` and can be used for PIIS consent creation in CMS-ASPSP-API 
