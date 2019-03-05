# Release notes v.2.0.1

## Bugfix: added validation for incorrect dates in periodic payments creation      

Now while creating new periodic payment its start date and end date are validated:
 - start date can not be in the past
 - end date can not be earlier than start date
 
Also while creating the future payment its execution date is validated and it can not be in the past.
 
In all above cases response with error `400 PERIOD_INVALID` is returned.
