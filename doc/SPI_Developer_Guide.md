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

### Implementation of PaymentSpi(s)
  
#### SCA Approach REDIRECT

#### SCA Approach DECOUPLED
!!! For the Decoupled and Embedded Approaches SPI developers have only to implement **PaymentAuthorisationSpi** that extends **AuthorisationSpi** (Description please see: *Implementation of PaymentSpi(s)* ) 

* The authorisation of Payment in this case is only with an implicit start of the authorisation process
* ASPSP is asking the Psu to authorise the payment via dedicated mobile app, or any other application or device which is independent from the online banking frontend.
* The ASPSP is asking the TPP to inform the PSU abut this authentication by sending a corresponding PSU Message like _Please use your xxx App to authorise the payment_.
* After the SCA having been processed between ASPSP and PSU, the TPP then needs to ask for the result of the transaction.


* In case of implicit start the authorisation will be used based on tppExplicitAuthorisationPreferred and signingBasketSupported values.
    * Implicit authorisation will be used in all the cases where tppExplicitAuthorisationPreferred or signingBasketSupported not equals true
    * Implicit approach is impossible in case of multilevel SCA
        * **tppExplicitAuthorisationPreferred**: value of tpp's choice of authorisation method
        * **signingBasketSupported**: Reads if signing basket supported from ASPSP profile service. It is true if ASPSP supports signing basket , false if doesn't
        * **multilevelScaRequired**: does response have multilevel SCA
      
    
* For Decoupled approach the developer need to implement the **PisScaAuthorisationService**. This contains following methods: 

    * createCommonPaymentAuthorisation: this method will create payment authorisation response and contains:
        * **paymentId**: ASPSP identifier of a payment,
        * **paymentType**: e.g. single payment, periodic payment, bulk payment
        * **psudata**: PsuIdData container of authorisation data about PSU
        
    * updateCommonPaymentPsuData: this method provides transporting data when updating consent psu data. For the Decoupled Approach this method is only applicable for 
    adding the psu identification, if not provided yet in the payment initiation request and for tyhe selection of authentication methods. It contains **request** with following data: 
    
    | Attribute              |Type                 | Description         |
    |:------------------     |:-------------------:| :-------------------|
    |paymentId               | String              | Resource identification of the related payment initiation             |
    |authorisationId         | String              | Resource identification if the related payment initiation, Signing Basket or Consent authorisation sub-resource |
    |scaAuthenticationData   | String              |SCA authentication data, depending on the chosen authentication method|
    |psuData                 | String              | e.g. PsuId, PsuIdType, PsuCorporateId and PsuCorporateIdType  |
    |password                | PSU Data             | Password of the psu             |
    |authenticationMethodId  | String              | The authentication method ID as provided by the ASPSP.              |
    |scaStatus               | Sca Status          | e.g. psuIdentified              |
    |paymentService          | String              | e.g. "payments", "bulk-payments" and "periodic-payments"              |
    |paymentProduct          | String              | The related payment product of the payment initiation to be authorized             |
    |updatePsuidentification | href Type              | The link to the payment initiation, which needs to be updated by the PSU identification if not delivered yet|
      
    
   * getAuthorisationSubResources with the **paymentId** and returns authorisation sub resources (e.g. a list of authorisation ids)
    
   * getAuthorisationScaStatus with **paymentId** (ASPSP identifier of the payment, associated with the authorisation) and 
   **authorisationId** (authorisation identifier). This method will return SCA status of the authorisation. 
        * example of Sca Status: 
            * RECEIVED("received", false): if an authorisation or cancellation-authorisation resource has been created successfully.
            * PSUIDENTIFIED("psuIdentified", false): if the PSU related to the authorisation or cancellation-authorisation resource has been identified.
    
   * getScaApproachServiceTypeProvider: to get sca approach used in current service. This will return the ScaApproach **"Decoupled"**
    
    
* Decoupled Approach for Payment cancellation

    * **createCommonPaymentCancellationAuthorisation**: This will create payment cancellation authorisation with **paymentId**, **paymentType** and **psudata**
    * **getCancellationAuthorisationSubResources**: with the **paymentId**
    * **updateCommonPaymentCancellationPsuData**: updates the cancellation for the payment
    * **getCancellationAuthorisationScaStatus** with **PaymentId** and **CancellationId**
    * **getScaApproachServiceTypeProvider**: to get sca approach used in current service. This will return the ScaApproach **“Decoupled”**
    

#### SCA Approach EMBEDDED

!!! For the Decoupled and Embedded Approaches SPI developers have only to implement **PaymentAuthorisationSpi** that extends **AuthorisationSpi** (Description please see: *Implementation of PaymentSpi(s)* )

For Embedded approach the developer need to implement the **PisScaAuthorisationService**. This contains following methods: 

   * createCommonPaymentAuthorisation: this method will create payment authorisation response and contains:
        * **paymentId**: ASPSP identifier of a payment,
        * **paymentType**: e.g. single payment, periodic payment, bulk payment
        * **psudata**: PsuIdData container of authorisation data about PSU
        
   * updateCommonPaymentPsuData: this method provides transporting data when updating consent psu data. For the Embedded Approach this method might be used to add credentials as a first factor authentication
   data of the psu and to select the authentication method. It contains **request** with following data: 
   
   | Attribute              |Type                 | Description         |
   |:------------------     |:-------------------:| :-------------------|
   |paymentId               | String              | Resource identification of the related payment initiation             |
   |authorisationId         | String              | Resource identification if the related payment initiation, Signing Basket or Consent authorisation sub-resource |
   |scaAuthenticationData   | String              |SCA authentication data, depending on the chosen authentication method|
   |psuData                 | String              | e.g. PsuId, PsuIdType, PsuCorporateId and PsuCorporateIdType  |
   |password                | PSU Data             | Password of the psu             |
   |authenticationMethodId  | String              | The authentication method ID as provided by the ASPSP.              |
   |scaStatus               | Sca Status          | e.g. psuIdentified              |
   |paymentService          | String              | e.g. "payments", "bulk-payments" and "periodic-payments"              |
   |paymentProduct          | String              | The related payment product of the payment initiation to be authorized             |
   |updatePsuidentification | href Type              | The link to the payment initiation, which needs to be updated by the PSU identification if not delivered yet|
     
  
   - This method will return the update payment authorization response, which contains **paymentId**, **authorisationId**, **SCA Status**, **psu message** and **links**. 
      
    
   * getAuthorisationSubResources with the **paymentId** and returns authorisation sub resources (e.g. a list of authorisation ids)
    
   * getAuthorisationScaStatus with **paymentId** (ASPSP identifier of the payment, associated with the authorisation) and 
     **authorisationId** (authorisation identifier). This method will return SCA status of the authorisation. 
          * example of Sca Status: 
              * RECEIVED("received", false): if an authorisation or cancellation-authorisation resource has been created successfully.
              * PSUIDENTIFIED("psuIdentified", false): if the PSU related to the authorisation or cancellation-authorisation resource has been identified.
    
   * getScaApproachServiceTypeProvider: to get sca approach used in current service. This will return the ScaApproach **"Embedded"**
    
    
* Embedded Approach for Payment cancellation

    * **createCommonPaymentCancellationAuthorisation**: This will create payment cancellation authorisation with **paymentId**, **paymentType** and **psudata**
    * **getCancellationAuthorisationSubResources**: with the **paymentId**
    * **updateCommonPaymentCancellationPsuData**: updates the cancellation for the payment
    * **getCancellationAuthorisationScaStatus** with **PaymentId** and **CancellationId**
    * **getScaApproachServiceTypeProvider**: to get sca approach used in current service. This will return the ScaApproach **“Embedded”**


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
