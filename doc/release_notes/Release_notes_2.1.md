# Release notes v.2.1

## Bugfix: changed response codes for wrong TAN

Now the response for AIS authorisation and Payment Cancellation authorisation when XS2A receives wrong PSU password or wrong TAN is
`PSU_CREDENTIALS_INVALID` instead of `FORMAT_ERROR`.
