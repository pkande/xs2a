# SPI Developer Guide

## Purpose and Scope

## Choosing the deployment layout

### Microservices Deployment

### Embedded Deployment

### Embedding XS2A Library

### Embedding CMS Library

### Embedding Profile library

## Setting up ASPSP Profile options

### Using debug interface

## Implementing SPI-API

### General requirements

#### SpiResponse

#### Work with ASPSP-Consent-Data object

### Implementation of AisConsentSpi

#### Providing account resources to consent 

#### SCA Approach REDIRECT

#### SCA Approach DECOUPLED

#### SCA Approach EMBEDDED

### Implementation of AccountSpi
SPI developer have to implement the following methods:

* **requestAccountList**: aims to request a list of account details. It contains:
    * **SpiContextData**: (PsuData and TppInfo)
    * **withBalance**: boolean representing if the responded AccountDetails should contain balance
    * **SpiAccountConsent**: This contains *psuData*, *tppInfo*, **AisConsentRequestType** (e.g. Global, All_Available_Accounts, Dedicated_Accounts or Bank_Offered) and the following:
    
     | Attribute            |  Type   | Description       |
     | :---                 |  :---:  |             :---     |
     | access               |  Account Access   | Requested access services.       |
     | recurringIndicator   |  Boolean    | true, if the consent is for recurring access to the account data.false, if the consent is for one access to the account data     |
     | validUntil           |  ISODate      | This parameter is requesting a valid until date for the requested consent. The consent is the local ASPSP date in ISOdate Format, e.g. 2017-10-30|
     | frequencyPerDay      |  Integer      | This field indicates the requested maximum frequency for an access per day.      |
     | lastActionDate       |  ISODate      | This date is containing the date of the last action on the consent object either through the XS2A interface or the PSU/ASPSP interface having an impact on the status       |
     | consentStatus        |  Consent Status      | authentication status of the consent      |
     | withBalance          |  Boolean       | If contained, this function reads the list of accessible payment accounts including the booking balance, if granted by the PSU in the related consent and available by the ASPSP.       |
     | tppRedirectPreferred |  Boolean       | If it equals "true", the TPP prefers a redirect over an embedded SCA approach. If it equals "false", the TPP prefers not be redirected for SCA.  |
     
    * **AspspConsentData**: Encrypted data that may be stored in the consent management system in the consent linked to a request. May be null if consent does not contain such data, or request isn't done from a workflow with a consent
    
* **requestAccountDetailForAccount**: aims to request an account detail for account and contains: 
    * **SpiContextData**
    * **withBalance**
    * **SpiAccountReference**: This type is containing any account identification which can be used on payload-level to address specific accounts.
    
    | Attribute            |  Type         | Description          |
    | :---                 |  :---:        |   :---               |
    | aspspAccountId       |  --           |                      |
    | resourceId           |  --           |                      |  
    | iban                 |  IBAN         |                      |
    | bban                 |  BBAN         | This data elements is used for payment accounts which have no IBAN     |
    | pan                  |  Max35Text    | Primary Account Number (PAN) of a card, can be tokenised by the ASPSP due to PCI DSS requirements   |
    | maskedPan            |  Max35Text    | Primary Account Number (PAN) of a card in a masked form.    |
    | msisdn               |  Max35Text    | An alias to access a payment account via a registered mobile phone number    |
    | currency             |  Currency Code| ISO 4217 Alpha 3 currency code    |
         
    * **SpiAccountConsent**
    * **AspspConsentData** 
    
* **requestTransactionsForAccount**: aims to request a list of transactions and contains: 
    * **SpiContextData**
    * **acceptMediaType**: requested by TPP response media type e.g. text/plain. Shall be propagated to response. This string may contain several content-types according to HTTP "Accept"-Header format.
    If desired media type is not possible to provide, NOT_SUPPORTED error to be returned. To provide formats other than JSON, use {@link SpiTransactionReport#transactionsRaw}
    * **withBalance**: boolean representing if the responded AccountDetails should contain balance
    * **DateFrom**: Date representing the beginning of the search period
    * **DateTo**: Date representing the ending of the search period
    * **SpiAccountReference**: 
    * **SpiAccountConsent**: 
    * **AspspConsentData**
    
* **requestTransactionForAccountByTransactionId**: aims to request a transaction by transactionId and contains: 
    * **SpiContextData**, **transactionId** (String representation of SPSP transaction primary identifier), **SpiAccountConsent** and **AspspConsentData**

* **requestBalancesForAccount**: aims to request a list of account balances and contains: 
    * **SpiContextData**, **SpiAccountReference**, **SpiAccountConsent** and **AspspConsentData**

### Implementation of PaymentSpi(s)
  
#### SCA Approach REDIRECT

#### SCA Approach DECOUPLED

#### SCA Approach EMBEDDED

### Implementation of FundsConfirmationSpi

## Working with CMS

### Using the CMS-PSU-API

#### SCA Approaches REDIRECT and DECOUPLED

#### SCA Approach EMBEDDED

### Using the CMS-ASPSP-API

#### Using the Tpp locking interface

#### Using the Consents/Payments export interface

#### Using the FundsConfirmation Consent interface

## Special modes

### Multi-tenancy support
