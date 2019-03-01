# Release notes v.2.0.1

## Bugfix: changed the response code for getting payment by ID with wrong payment service      

Before the response for getting payment details by its ID with wrong payment service in path (GET `/v1/wrong-payment-service/sepa-credit-transfers/payment_id`)
was returning `400 - Bad Request`. From now on this response is: `404 - Resource Unknown`.
