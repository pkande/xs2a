/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.adorsys.psd2.consent.web.xs2a.controller;


import de.adorsys.psd2.consent.api.CmsScaMethod;
import de.adorsys.psd2.consent.api.ais.*;
import de.adorsys.psd2.consent.api.service.AisConsentAuthorisationServiceEncrypted;
import de.adorsys.psd2.consent.api.service.AisConsentServiceEncrypted;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.sca.AuthorisationScaApproachResponse;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

//TODO Rename consentId to encryptedConsentId https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/705
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "api/v1/ais/consent")
@Api(value = "api/v1/ais/consent", tags = "AIS, Consents", description = "Provides access to consent management system for AIS")
public class AisConsentController {
    private final AisConsentAuthorisationServiceEncrypted aisConsentAuthorisationServiceEncrypted;
    private final AisConsentServiceEncrypted aisConsentService;

    @PostMapping(path = "/")
    @ApiOperation(value = "Create consent for given psu id and accesses.")
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "Created", response = String.class),
        @ApiResponse(code = 204, message = "No Content")})
    public ResponseEntity<CreateAisConsentResponse> createConsent(@RequestBody CreateAisConsentRequest request) {
        return aisConsentService.createConsent(request)
                   .map(consentId -> new ResponseEntity<>(new CreateAisConsentResponse(consentId), HttpStatus.CREATED))
                   .orElseGet(() -> new ResponseEntity<>(HttpStatus.NO_CONTENT));
    }

    @PostMapping(path = "/action")
    @ApiOperation(value = "Save information about uses of consent")
    public ResponseEntity<Void> saveConsentActionLog(@RequestBody AisConsentActionRequest request) {
        aisConsentService.checkConsentAndSaveActionLog(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping(path = "/{consent-id}")
    @ApiOperation(value = "Read account consent by given consent id.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = AisAccountConsent.class),
        @ApiResponse(code = 204, message = "No Content")})
    public ResponseEntity<AisAccountConsent> getConsentById(
        @ApiParam(name = "consent-id", value = "The account consent identification assigned to the created account consent.", example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7")
        @PathVariable("consent-id") String consentId) {
        return aisConsentService.getAisAccountConsentById(consentId)
                   .map(consent -> new ResponseEntity<>(consent, HttpStatus.OK))
                   .orElseGet(() -> new ResponseEntity<>(HttpStatus.NO_CONTENT));
    }

    @GetMapping(path = "/initial/{consent-id}")
    @ApiOperation(value = "Read account consent by given initial consent id.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = AisAccountConsent.class),
        @ApiResponse(code = 204, message = "No Content")})
    public ResponseEntity<AisAccountConsent> getInitialConsentById(
        @ApiParam(name = "consent-id", value = "The account initial consent identification assigned to the created account consent.", example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7")
        @PathVariable("consent-id") String consentId) {
        return aisConsentService.getInitialAisAccountConsentById(consentId)
                   .map(consent -> new ResponseEntity<>(consent, HttpStatus.OK))
                   .orElseGet(() -> new ResponseEntity<>(HttpStatus.NO_CONTENT));
    }

    @PutMapping(path = "/{consent-id}/access")
    @ApiOperation(value = "Update AccountAccess in the consent identified by given consent id.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 404, message = "Not Found")})
    public ResponseEntity<CreateAisConsentResponse> updateAccountAccess(
        @ApiParam(name = "consent-id", value = "The account consent identification assigned to the created account consent.", example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7")
        @PathVariable("consent-id") String consentId,
        @RequestBody AisAccountAccessInfo request) {
        return aisConsentService.updateAspspAccountAccess(consentId, request)
                   .map(consentIdUpdated -> new ResponseEntity<>(new CreateAisConsentResponse(consentIdUpdated), HttpStatus.OK))
                   .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping(path = "/{consent-id}/status")
    @ApiOperation(value = "Can check the status of an account information consent resource.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = ConsentStatus.class),
        @ApiResponse(code = 404, message = "Not Found")})
    public ResponseEntity<AisConsentStatusResponse> getConsentStatusById(
        @ApiParam(name = "consent-id", value = "The account consent identification assigned to the created account consent.", example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7")
        @PathVariable("consent-id") String consentId) {
        return aisConsentService.getConsentStatusById(consentId)
                   .map(status -> new ResponseEntity<>(new AisConsentStatusResponse(status), HttpStatus.OK))
                   .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PutMapping(path = "/{consent-id}/status/{status}")
    @ApiOperation(value = "Update consent status in the consent identified by given consent id.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 404, message = "Not Found")})
    public ResponseEntity<Void> updateConsentStatus(
        @ApiParam(name = "consent-id", value = "The account consent identification assigned to the created account consent.", example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7")
        @PathVariable("consent-id") String consentId,
        @ApiParam(value = "The following code values are permitted 'VALID', 'REJECTED', 'REVOKED_BY_PSU', 'TERMINATED_BY_TPP'. These values might be extended by ASPSP by more values.", example = "VALID")
        @PathVariable("status") String status) {
        return aisConsentService.updateConsentStatusById(consentId, ConsentStatus.valueOf(status))
                   ? new ResponseEntity<>(HttpStatus.OK)
                   : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @DeleteMapping(path = "/{consent-id}/old-consents")
    @ApiOperation(value = "Find old consents for current TPP and PSU and terminates them")
    @ApiResponse(code = 204, message = "No Content")
    public ResponseEntity<Void> findAndTerminateOldConsentsByNewConsentId(
        @ApiParam(name = "consent-id", value = "The account consent identification assigned to the new account consent.", example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7")
        @PathVariable("consent-id") String consentId) {
        aisConsentService.findAndTerminateOldConsentsByNewConsentId(consentId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(path = "/{consent-id}/authorizations")
    @ApiOperation(value = "Create consent authorization for given consent id.")
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "Created"),
        @ApiResponse(code = 404, message = "Not Found")})
    public ResponseEntity<CreateAisConsentAuthorizationResponse> createConsentAuthorization(
        @ApiParam(name = "consent-id", value = "The consent identification assigned to the created consent authorization.", example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7")
        @PathVariable("consent-id") String consentId,
        @RequestBody AisConsentAuthorizationRequest consentAuthorization) {
        return aisConsentAuthorisationServiceEncrypted.createAuthorizationWithResponse(consentId, consentAuthorization)
                   .map(auth -> new ResponseEntity<>(auth, HttpStatus.CREATED))
                   .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PutMapping(path = "/authorizations/{authorization-id}")
    @ApiOperation(value = "Update consent authorization.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 404, message = "Not Found")})
    public ResponseEntity<Void> updateConsentAuthorization(
        @ApiParam(name = "authorization-id", value = "The consent authorization identification assigned to the created authorization.", example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7")
        @PathVariable("authorization-id") String authorizationId,
        @ApiParam(value = "The following code values are permitted 'VALID', 'REJECTED', 'REVOKED_BY_PSU', 'TERMINATED_BY_TPP'. These values might be extended by ASPSP by more values.", example = "VALID")
        @RequestBody AisConsentAuthorizationRequest consentAuthorization) {
        return aisConsentAuthorisationServiceEncrypted.updateConsentAuthorization(authorizationId, consentAuthorization)
                   ? new ResponseEntity<>(HttpStatus.OK)
                   : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @GetMapping(path = "/{consent-id}/authorizations/{authorization-id}")
    @ApiOperation(value = "Getting consent authorization.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 404, message = "Not Found")})
    public ResponseEntity<AisConsentAuthorizationResponse> getConsentAuthorization(
        @ApiParam(name = "consent-id", value = "The account consent identification assigned to the created account consent.", example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7")
        @PathVariable("consent-id") String consentId,
        @ApiParam(name = "authorization-id", value = "The consent authorization identification assigned to the created authorization.", example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7")
        @PathVariable("authorization-id") String authorizationId) {

        return aisConsentAuthorisationServiceEncrypted.getAccountConsentAuthorizationById(authorizationId, consentId)
                   .map(resp -> new ResponseEntity<>(resp, HttpStatus.OK))
                   .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping(path = "/{consent-id}/authorisations/{authorisation-id}/status")
    @ApiOperation(value = "Gets SCA status of consent authorisation.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 404, message = "Not Found")})
    public ResponseEntity<ScaStatus> getConsentAuthorizationScaStatus(
        @ApiParam(name = "consent-id", value = "Account consent identification.", example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7")
        @PathVariable("consent-id") String consentId,
        @ApiParam(name = "authorisation-id", value = "Consent authorisation identification", example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7")
        @PathVariable("authorisation-id") String authorisationId) {

        return aisConsentAuthorisationServiceEncrypted.getAuthorisationScaStatus(consentId, authorisationId)
                   .map(resp -> new ResponseEntity<>(resp, HttpStatus.OK))
                   .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping(path = "/{consent-id}/authorisations")
    @ApiOperation(value = "Gets list of consent authorisation IDs by consent ID")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 404, message = "Not Found")})
    public ResponseEntity<List<String>> getConsentAuthorisation(
        @ApiParam(name = "consent-id", value = "The account consent identification assigned to the created account consent.", example = "vOHy6fj2f5IgxHk-kTlhw6sZdTXbRE3bWsu2obq54beYOChP5NvRmfh06nrwumc2R01HygQenchEcdGOlU-U0A==_=_iR74m2PdNyE")
        @PathVariable("consent-id") String consentId) {
        return aisConsentAuthorisationServiceEncrypted.getAuthorisationsByConsentId(consentId)
                   .map(authorisation -> new ResponseEntity<>(authorisation, HttpStatus.OK))
                   .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping(path = "/authorisations/{authorisation-id}/authentication-methods/{authentication-method-id}")
    @ApiOperation(value = "Checks if requested authentication method is decoupled")
    @ApiResponse(code = 200, message = "OK")
    public ResponseEntity<Boolean> isAuthenticationMethodDecoupled(
        @ApiParam(name = "authorisation-id", value = "Consent authorisation identification", example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7")
        @PathVariable("authorisation-id") String authorisationId,
        @ApiParam(name = "authentication-method-id", value = "Authentication method identification", example = "sms")
        @PathVariable("authentication-method-id") String authenticationMethodId) {
        boolean isMethodDecoupled = aisConsentAuthorisationServiceEncrypted.isAuthenticationMethodDecoupled(authorisationId, authenticationMethodId);
        return new ResponseEntity<>(isMethodDecoupled, HttpStatus.OK);
    }

    @PostMapping(path = "/authorisations/{authorisation-id}/authentication-methods")
    @ApiOperation(value = "Saves authentication methods in authorisation")
    @ApiResponses(value = {
        @ApiResponse(code = 204, message = "No Content"),
        @ApiResponse(code = 404, message = "Not Found")})
    public ResponseEntity<Void> saveAuthenticationMethods(
        @ApiParam(name = "authorisation-id", value = "The consent authorisation identification.", example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7")
        @PathVariable("authorisation-id") String authorisationId,
        @RequestBody List<CmsScaMethod> methods) {
        return aisConsentAuthorisationServiceEncrypted.saveAuthenticationMethods(authorisationId, methods)
                   ? new ResponseEntity<>(HttpStatus.NO_CONTENT)
                   : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PutMapping(path = "/authorisations/{authorisation-id}/sca-approach/{sca-approach}")
    @ApiOperation(value = "Updates AIS SCA approach in authorisation")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 404, message = "Not Found")})
    public ResponseEntity<Boolean> updateScaApproach(
        @ApiParam(name = "authorisation-id", value = "The consent authorisation identification.", example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7")
        @PathVariable("authorisation-id") String authorisationId,
        @ApiParam(name = "sca-approach", value = "Chosen SCA approach.", example = "REDIRECT")
        @PathVariable("sca-approach") ScaApproach scaApproach) {
        return aisConsentAuthorisationServiceEncrypted.updateScaApproach(authorisationId, scaApproach)
                   ? new ResponseEntity<>(true, HttpStatus.OK)
                   : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PutMapping(path = "/{consent-id}/multilevel-sca")
    @ApiOperation(value = "Updates multilevel SCA in consent")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 404, message = "Not Found")})
    public ResponseEntity<Boolean> updateMultilevelScaRequired(
        @ApiParam(name = "consent-id", value = "The consent identification.", example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7")
        @PathVariable("consent-id") String consentId,
        @ApiParam(name = "multilevel-sca", value = "Multilevel SCA.", example = "false")
        @RequestParam(value = "multilevel-sca", defaultValue = "false") boolean multilevelSca) {
        return aisConsentService.updateMultilevelScaRequired(consentId, multilevelSca)
                   ? new ResponseEntity<>(true, HttpStatus.OK)
                   : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    // TODO correct swagger docs https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/722
    @GetMapping(path = "/authorisations/{authorisation-id}/sca-approach")
    @ApiOperation(value = "Gets list of consent authorisation IDs by consent ID")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 404, message = "Not Found")})
    public ResponseEntity<AuthorisationScaApproachResponse> getAuthorisationScaApproach(
        @ApiParam(name = "consent-id", value = "The account consent identification assigned to the created account consent.", example = "vOHy6fj2f5IgxHk-kTlhw6sZdTXbRE3bWsu2obq54beYOChP5NvRmfh06nrwumc2R01HygQenchEcdGOlU-U0A==_=_iR74m2PdNyE")
        @PathVariable("authorisation-id") String authorisationId) {
        return aisConsentAuthorisationServiceEncrypted.getAuthorisationScaApproach(authorisationId)
                   .map(scaApproachResponse -> new ResponseEntity<>(scaApproachResponse, HttpStatus.OK))
                   .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
}
