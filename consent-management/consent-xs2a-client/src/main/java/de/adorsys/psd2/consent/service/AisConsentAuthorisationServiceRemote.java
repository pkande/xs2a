/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
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

package de.adorsys.psd2.consent.service;

import de.adorsys.psd2.consent.api.CmsScaMethod;
import de.adorsys.psd2.consent.api.ais.AisConsentAuthorizationRequest;
import de.adorsys.psd2.consent.api.ais.AisConsentAuthorizationResponse;
import de.adorsys.psd2.consent.api.ais.CreateAisConsentAuthorizationResponse;
import de.adorsys.psd2.consent.api.service.AisConsentAuthorisationServiceEncrypted;
import de.adorsys.psd2.consent.config.AisConsentRemoteUrls;
import de.adorsys.psd2.consent.config.CmsRestException;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.sca.AuthorisationScaApproachResponse;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

// TODO discuss error handling (e.g. 400 HttpCode response) https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/498
@Slf4j
@Service
@RequiredArgsConstructor
public class AisConsentAuthorisationServiceRemote implements AisConsentAuthorisationServiceEncrypted {
    @Qualifier("consentRestTemplate")
    private final RestTemplate consentRestTemplate;
    private final AisConsentRemoteUrls remoteAisConsentUrls;

    @Override
    public Optional<String> createAuthorization(String consentId, AisConsentAuthorizationRequest request) {
        CreateAisConsentAuthorizationResponse response = consentRestTemplate.postForEntity(remoteAisConsentUrls.createAisConsentAuthorization(),
                                                                                           request, CreateAisConsentAuthorizationResponse.class, consentId).getBody();

        return Optional.ofNullable(response)
                   .map(CreateAisConsentAuthorizationResponse::getAuthorizationId);
    }

    @Override
    public Optional<CreateAisConsentAuthorizationResponse> createAuthorizationWithResponse(String consentId, AisConsentAuthorizationRequest request) {
        CreateAisConsentAuthorizationResponse response = consentRestTemplate.postForEntity(remoteAisConsentUrls.createAisConsentAuthorization(),
                                                                                           request, CreateAisConsentAuthorizationResponse.class, consentId).getBody();

        return Optional.ofNullable(response);
    }

    @Override
    public Optional<AisConsentAuthorizationResponse> getAccountConsentAuthorizationById(String authorizationId, String consentId) {
        return Optional.ofNullable(consentRestTemplate.getForEntity(remoteAisConsentUrls.getAisConsentAuthorizationById(), AisConsentAuthorizationResponse.class, consentId, authorizationId)
                                       .getBody());
    }

    @Override
    public boolean updateConsentAuthorization(String authorizationId, AisConsentAuthorizationRequest request) {
        consentRestTemplate.put(remoteAisConsentUrls.updateAisConsentAuthorization(), request, authorizationId);
        return true;
    }

    @Override
    public Optional<List<String>> getAuthorisationsByConsentId(String encryptedConsentId) {
        try {
            ResponseEntity<List<String>> request = consentRestTemplate.exchange(
                remoteAisConsentUrls.getAuthorisationSubResources(), HttpMethod.GET, null, new ParameterizedTypeReference<List<String>>() {
                }, encryptedConsentId);
            return Optional.ofNullable(request.getBody());
        } catch (CmsRestException cmsRestException) {
            log.warn("No authorisation found by consentId {}", encryptedConsentId);
        }
        return Optional.empty();
    }

    @Override
    public Optional<ScaStatus> getAuthorisationScaStatus(String encryptedConsentId, String authorisationId) {
        try {
            ResponseEntity<ScaStatus> request = consentRestTemplate.getForEntity(
                remoteAisConsentUrls.getAuthorisationScaStatus(), ScaStatus.class, encryptedConsentId, authorisationId);
            return Optional.ofNullable(request.getBody());
        } catch (CmsRestException cmsRestException) {
            log.warn("Couldn't get authorisation SCA Status by consentId {} and authorisationId {}");
        }
        return Optional.empty();
    }

    @Override
    public boolean isAuthenticationMethodDecoupled(String authorisationId, String authenticationMethodId) {
        return consentRestTemplate.getForEntity(remoteAisConsentUrls.isAuthenticationMethodDecoupled(), Boolean.class, authorisationId, authenticationMethodId)
                   .getBody();
    }

    @Override
    public boolean saveAuthenticationMethods(String authorisationId, List<CmsScaMethod> methods) {
        try {
            ResponseEntity<Void> responseEntity = consentRestTemplate.exchange(remoteAisConsentUrls.saveAuthenticationMethods(), HttpMethod.POST, new HttpEntity<>(methods), Void.class, authorisationId);

            if (responseEntity.getStatusCode() == HttpStatus.NO_CONTENT) {
                return true;
            }
        } catch (CmsRestException cmsRestException) {
            log.warn("Couldn't save authentication methods {} by authorisationId {}", methods, authorisationId);
        }

        return false;
    }

    @Override
    public boolean updateScaApproach(String authorisationId, ScaApproach scaApproach) {
        return consentRestTemplate.exchange(remoteAisConsentUrls.updateScaApproach(), HttpMethod.PUT, null, Boolean.class, authorisationId, scaApproach)
                   .getBody();
    }

    @Override
    public Optional<AuthorisationScaApproachResponse> getAuthorisationScaApproach(String authorisationId) {
        try {
            ResponseEntity<AuthorisationScaApproachResponse> request = consentRestTemplate.getForEntity(
                remoteAisConsentUrls.getAuthorisationScaApproach(), AuthorisationScaApproachResponse.class, authorisationId);
            return Optional.ofNullable(request.getBody());
        } catch (CmsRestException cmsRestException) {
            log.warn("Couldn't get authorisation SCA Approach by authorisationId {}", authorisationId);
        }

        return Optional.empty();
    }
}
