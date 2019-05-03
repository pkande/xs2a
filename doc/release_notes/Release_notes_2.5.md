# Release notes v.2.5

## Table of Contents
- Bugfix: Wrong error code "requestedExecutionDate" value in the past

## Bugfix: Wrong error code "requestedExecutionDate" value in the past
Error code was changed to `400 EXECUTION_DATE_INVALID` from `400 FORMAT_ERROR` when `requestedExecutionDate` field is less then current date.
