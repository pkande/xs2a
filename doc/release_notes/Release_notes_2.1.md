# Release notes v.2.1

## Added authorisation type to response for getting PSU data authorisations

Now these endpoints: `/v1/payment/{payment-id}/authorisation/psus` and `/v1/ais/consent/{consent-id}/authorisation/psus` have enriched
responses with new field added - `authorisationType`. The value can be `CREATED` or `CANCELLED` by now.
