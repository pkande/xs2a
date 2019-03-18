# Release notes v.2.2

## Bugfix: Add `Location` header and `self` link to the AIS consent creation response
From now on, response to the AIS consent creation request(`POST /v1/consents`) contains `Location` header and `self` 
link. The value of this header and link points to the resource that was created by this request.
