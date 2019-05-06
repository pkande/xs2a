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

package de.adorsys.psd2.xs2a.service.authorization.pis;

import de.adorsys.psd2.consent.api.CmsAuthorisationType;
import de.adorsys.psd2.consent.api.pis.authorisation.CreatePisAuthorisationRequest;
import de.adorsys.psd2.consent.api.pis.authorisation.CreatePisAuthorisationResponse;
import de.adorsys.psd2.consent.api.pis.authorisation.GetPisAuthorisationResponse;
import de.adorsys.psd2.consent.api.pis.authorisation.UpdatePisCommonPaymentPsuDataRequest;
import de.adorsys.psd2.consent.api.service.PisCommonPaymentServiceEncrypted;
import de.adorsys.psd2.xs2a.config.factory.PisScaStageAuthorisationFactory;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.AuthorisationScaApproachResponse;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.domain.MessageErrorCode;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataRequest;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataResponse;
import de.adorsys.psd2.xs2a.domain.pis.PaymentAuthorisationType;
import de.adorsys.psd2.xs2a.service.InitialScaApproachResolver;
import de.adorsys.psd2.xs2a.service.authorization.pis.stage.PisScaStage;
import de.adorsys.psd2.xs2a.service.mapper.consent.Xs2aPisCommonPaymentMapper;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
// TODO this class takes low-level communication to Consent-management-system. Should be migrated to consent-services package. All XS2A business-logic should be removed from here to XS2A services. https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/332
public class PisAuthorisationService {
    private final PisCommonPaymentServiceEncrypted pisCommonPaymentServiceEncrypted;
    private final PisScaStageAuthorisationFactory pisScaStageAuthorisationFactory;
    private final Xs2aPisCommonPaymentMapper pisCommonPaymentMapper;
    private final InitialScaApproachResolver initialScaApproachResolver;

    /**
     * Sends a POST request to CMS to store created pis authorisation
     *
     * @param paymentId String representation of identifier of stored payment
     * @param psuData   PsuIdData container of authorisation data about PSU
     * @return a response object containing authorisation id
     */
    public CreatePisAuthorisationResponse createPisAuthorisation(String paymentId, PsuIdData psuData) {
        CreatePisAuthorisationRequest request = new CreatePisAuthorisationRequest(CmsAuthorisationType.CREATED, psuData, initialScaApproachResolver.resolveScaApproach());
        return pisCommonPaymentServiceEncrypted.createAuthorization(paymentId, request)
                   .orElse(null);
    }

    /**
     * Updates PIS authorisation according to psu's sca methods with embedded and decoupled SCA approach
     *
     * @param request     Provides transporting data when updating pis authorisation
     * @param scaApproach current SCA approach, preferred by the server
     * @return update pis authorisation response, which contains payment id, authorisation id, sca status, psu message and links
     */
    public Xs2aUpdatePisCommonPaymentPsuDataResponse updatePisAuthorisation(Xs2aUpdatePisCommonPaymentPsuDataRequest request, ScaApproach scaApproach) {
        String authorisationId = request.getAuthorisationId();
        Optional<GetPisAuthorisationResponse> pisAuthorisationOptional = pisCommonPaymentServiceEncrypted.getPisAuthorisationById(authorisationId);
        if (!pisAuthorisationOptional.isPresent()) {
            log.info("Authorisation ID: [{}]. Updating PIS authorisation PSU Data has failed: authorization is not found",
                     authorisationId);

            ErrorHolder errorHolder = ErrorHolder.builder(MessageErrorCode.RESOURCE_UNKNOWN_404)
                                          .errorType(ErrorType.PIS_404)
                                          .messages(Collections.singletonList("Pis authorization is not found"))
                                          .build();
            return new Xs2aUpdatePisCommonPaymentPsuDataResponse(errorHolder, request.getPaymentId(), authorisationId, request.getPsuData());

        }

        GetPisAuthorisationResponse response = pisAuthorisationOptional.get();

        PisScaStage<Xs2aUpdatePisCommonPaymentPsuDataRequest, GetPisAuthorisationResponse, Xs2aUpdatePisCommonPaymentPsuDataResponse> service =
            pisScaStageAuthorisationFactory.getService(PisScaStageAuthorisationFactory.getServiceName(scaApproach, response.getScaStatus()));
        Xs2aUpdatePisCommonPaymentPsuDataResponse stageResponse = service.apply(request, response);

        if (!stageResponse.hasError()) {
            doUpdatePisAuthorisation(pisCommonPaymentMapper.mapToCmsUpdateCommonPaymentPsuDataReq(stageResponse));
        }

        return stageResponse;
    }

    /**
     * Updates PIS cancellation authorisation according to psu's sca methods with embedded and decoupled SCA approach
     *
     * @param request     Provides transporting data when updating pis cancellation authorisation
     * @param scaApproach current SCA approach, preferred by the server
     * @return update pis authorisation response, which contains payment id, authorisation id, sca status, psu message and links
     */
    public Xs2aUpdatePisCommonPaymentPsuDataResponse updatePisCancellationAuthorisation(Xs2aUpdatePisCommonPaymentPsuDataRequest request, ScaApproach scaApproach) {
        String authorisationId = request.getAuthorisationId();
        Optional<GetPisAuthorisationResponse> pisCancellationAuthorisationOptional = pisCommonPaymentServiceEncrypted.getPisCancellationAuthorisationById(request.getAuthorisationId());
        if (!pisCancellationAuthorisationOptional.isPresent()) {
            log.info("Authorisation ID: [{}]. Updating PIS authorisation PSU Data has failed: authorization is not found",
                     authorisationId);

            ErrorHolder errorHolder = ErrorHolder.builder(MessageErrorCode.RESOURCE_UNKNOWN_404)
                                          .errorType(ErrorType.PIS_404)
                                          .messages(Collections.singletonList("Pis cancellation authorization is not found"))
                                          .build();
            return new Xs2aUpdatePisCommonPaymentPsuDataResponse(errorHolder, request.getPaymentId(), authorisationId, request.getPsuData());
        }

        GetPisAuthorisationResponse response = pisCancellationAuthorisationOptional.get();

        PisScaStage<Xs2aUpdatePisCommonPaymentPsuDataRequest, GetPisAuthorisationResponse, Xs2aUpdatePisCommonPaymentPsuDataResponse> service =
            pisScaStageAuthorisationFactory.getService(PisScaStageAuthorisationFactory.getCancellationServiceName(scaApproach, response.getScaStatus()));
        Xs2aUpdatePisCommonPaymentPsuDataResponse stageResponse = service.apply(request, response);

        if (!stageResponse.hasError()) {
            doUpdatePisCancellationAuthorisation(pisCommonPaymentMapper.mapToCmsUpdateCommonPaymentPsuDataReq(stageResponse));
        }

        return stageResponse;
    }

    public void doUpdatePisAuthorisation(UpdatePisCommonPaymentPsuDataRequest request) {
        pisCommonPaymentServiceEncrypted.updatePisAuthorisation(request.getAuthorizationId(), request);
    }

    public void doUpdatePisCancellationAuthorisation(UpdatePisCommonPaymentPsuDataRequest request) {
        pisCommonPaymentServiceEncrypted.updatePisCancellationAuthorisation(request.getAuthorizationId(), request);
    }

    /**
     * Sends a POST request to CMS to store created pis authorisation cancellation
     *
     * @param paymentId String representation of identifier of payment ID
     * @param psuData   PsuIdData container of authorisation data about PSU
     * @return long representation of identifier of stored pis authorisation cancellation
     */
    public CreatePisAuthorisationResponse createPisAuthorisationCancellation(String paymentId, PsuIdData psuData) {
        CreatePisAuthorisationRequest request = new CreatePisAuthorisationRequest(CmsAuthorisationType.CANCELLED, psuData, initialScaApproachResolver.resolveScaApproach());
        return pisCommonPaymentServiceEncrypted.createAuthorizationCancellation(paymentId, request)
                   .orElse(null);
    }

    /**
     * Sends a GET request to CMS to get cancellation authorisation sub resources
     *
     * @param paymentId String representation of identifier of payment ID
     * @return list of pis authorisation IDs
     */
    public Optional<List<String>> getCancellationAuthorisationSubResources(String paymentId) {
        return pisCommonPaymentServiceEncrypted.getAuthorisationsByPaymentId(paymentId, CmsAuthorisationType.CANCELLED);
    }

    /**
     * Sends a GET request to CMS to get authorisation sub resources
     *
     * @param paymentId String representation of identifier of payment ID
     * @return list of pis authorisation IDs
     */
    public Optional<List<String>> getAuthorisationSubResources(String paymentId) {
        return pisCommonPaymentServiceEncrypted.getAuthorisationsByPaymentId(paymentId, CmsAuthorisationType.CREATED);
    }

    /**
     * Gets SCA status of the authorisation
     *
     * @param paymentId       String representation of the payment identifier
     * @param authorisationId String representation of the authorisation identifier
     * @return SCA status of the authorisation
     */
    public Optional<ScaStatus> getAuthorisationScaStatus(String paymentId, String authorisationId) {
        return pisCommonPaymentServiceEncrypted.getAuthorisationScaStatus(paymentId, authorisationId, CmsAuthorisationType.CREATED);
    }

    /**
     * Gets SCA status of the cancellation authorisation
     *
     * @param paymentId      String representation of the payment identifier
     * @param cancellationId String representation of the cancellation authorisation identifier
     * @return SCA status of the authorisation
     */
    public Optional<ScaStatus> getCancellationAuthorisationScaStatus(String paymentId, String cancellationId) {
        return pisCommonPaymentServiceEncrypted.getAuthorisationScaStatus(paymentId, cancellationId, CmsAuthorisationType.CANCELLED);
    }

    /**
     * Gets SCA approach of the authorisation by its id and type
     *
     * @param authorisationId   String representation of the authorisation identifier
     * @param authorisationType Type of authorisation
     * @return SCA approach of the authorisation
     */
    public Optional<AuthorisationScaApproachResponse> getAuthorisationScaApproach(String authorisationId, PaymentAuthorisationType authorisationType) {
        CmsAuthorisationType cmsAuthorisationType = authorisationType == PaymentAuthorisationType.INITIATION
                                                        ? CmsAuthorisationType.CREATED
                                                        : CmsAuthorisationType.CANCELLED;

        return pisCommonPaymentServiceEncrypted.getAuthorisationScaApproach(authorisationId, cmsAuthorisationType);
    }
}
