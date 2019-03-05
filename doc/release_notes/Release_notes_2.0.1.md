
## Bugfix: Fixed the process of checking daily access limit for AIS consent
From now on when TPP exceeds allowed frequency per day for AIS consent, it will receive response with `ACCESS_EXCEEDED` error (response code HTTP 429).

## Bugfix: changed the response code for getting payment by ID with wrong payment service      

Before the response for getting payment details by its ID with wrong payment service in path (GET `/v1/wrong-payment-service/sepa-credit-transfers/payment_id`)
was returning `400 - Bad Request`. From now on this response is: `404 - Resource Unknown`.
