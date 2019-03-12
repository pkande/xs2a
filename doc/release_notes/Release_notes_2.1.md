# Release notes v.2.1

## Added transaction status for PIS

Now entities CmsPeriodicPayment and CmsSinglePayment store the transaction status of the payment. I.e. when ASPSP wants
to get payment by its ID - it will receive the transaction status among the other fields.
