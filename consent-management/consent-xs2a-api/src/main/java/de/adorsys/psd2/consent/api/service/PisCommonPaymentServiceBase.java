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

package de.adorsys.psd2.consent.api.service;

import de.adorsys.psd2.consent.api.CmsAuthorisationType;
import de.adorsys.psd2.consent.api.CmsScaMethod;
import de.adorsys.psd2.consent.api.pis.CreatePisCommonPaymentResponse;
import de.adorsys.psd2.consent.api.pis.authorisation.*;
import de.adorsys.psd2.consent.api.pis.proto.PisCommonPaymentRequest;
import de.adorsys.psd2.consent.api.pis.proto.PisCommonPaymentResponse;
import de.adorsys.psd2.consent.api.pis.proto.PisPaymentInfo;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.AuthorisationScaApproachResponse;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;

import java.util.List;
import java.util.Optional;

/**
 * Base version of PisCommonPaymentService that contains all method declarations.
 * Should not be implemented directly, consider using one of the interfaces that extends this one.
 *
 * @see PisCommonPaymentService
 * @see PisCommonPaymentServiceEncrypted
 */
interface PisCommonPaymentServiceBase {

    Optional<CreatePisCommonPaymentResponse> createCommonPayment(PisPaymentInfo request);

    /**
     * Retrieves common payment status from pis payment by payment identifier
     *
     * @param paymentId String representation of pis payment identifier
     * @return Information about the status of a common payment
     */
    Optional<TransactionStatus> getPisCommonPaymentStatusById(String paymentId);

    /**
     * Reads full information of pis payment by payment identifier
     *
     * @param paymentId String representation of pis payment identifier
     * @return Response containing full information about pis payment
     */
    Optional<PisCommonPaymentResponse> getCommonPaymentById(String paymentId);

    /**
     * Updates pis payment status by payment identifier
     *
     * @param paymentId String representation of pis payment identifier
     * @param status    new payment status
     * @return Response containing result of status changing
     */
    Optional<Boolean> updateCommonPaymentStatusById(String paymentId, TransactionStatus status);

    /**
     * Creates payment authorization
     *
     * @param paymentId String representation of the payment identifier
     * @param request   PIS authorisation request
     * @return Response containing authorization id
     */
    Optional<CreatePisAuthorisationResponse> createAuthorization(String paymentId, CreatePisAuthorisationRequest request);

    /**
     * Creates payment authorization cancellation
     *
     * @param paymentId               String representation of the payment identifier
     * @param pisAuthorisationRequest PIS authorisation request
     * @return Response containing authorization id
     */
    Optional<CreatePisAuthorisationResponse> createAuthorizationCancellation(String paymentId, CreatePisAuthorisationRequest pisAuthorisationRequest);

    /**
     * Updates payment authorization
     *
     * @param authorisationId String representation of the authorisation identifier
     * @param request         Incoming request for updating authorization
     * @return Response containing SCA status, available and chosen Sca method
     */
    Optional<UpdatePisCommonPaymentPsuDataResponse> updatePisAuthorisation(String authorisationId, UpdatePisCommonPaymentPsuDataRequest request);

    /**
     * Updates payment cancellation authorization
     *
     * @param authorizationId String representation of the authorisation identifier
     * @param request         Incoming request for updating authorization
     * @return Response containing SCA status, available and chosen Sca method
     */
    Optional<UpdatePisCommonPaymentPsuDataResponse> updatePisCancellationAuthorisation(String authorizationId, UpdatePisCommonPaymentPsuDataRequest request);

    /**
     * Updates PIS payment data and stores it into database
     *
     * @param request   PIS payment request for update payment data
     * @param paymentId Payment ID
     */
    void updateCommonPayment(PisCommonPaymentRequest request, String paymentId);

    /**
     * Get information about Authorisation by authorisation identifier
     *
     * @param authorisationId String representation of the authorisation identifier
     * @return Response containing information about Authorisation
     */
    Optional<GetPisAuthorisationResponse> getPisAuthorisationById(String authorisationId);

    /**
     * Get information about Authorisation by cancellation identifier
     *
     * @param cancellationId String representation of the cancellation identifier
     * @return Response containing information about Authorisation
     */
    Optional<GetPisAuthorisationResponse> getPisCancellationAuthorisationById(String cancellationId);

    /**
     * Gets list of payment authorisation IDs by payment ID and authorisation type
     *
     * @param paymentId         String representation of the payment identifier
     * @param authorisationType Type of authorisation
     * @return Response containing information about authorisation IDs
     */
    Optional<List<String>> getAuthorisationsByPaymentId(String paymentId, CmsAuthorisationType authorisationType);

    /**
     * Gets SCA status of the authorisation by payment ID, authorisation ID and authorisation type
     *
     * @param paymentId         String representation of the payment identifier
     * @param authorisationId   String representation of the authorisation identifier
     * @param authorisationType Type of authorisation
     * @return SCA status of the authorisation
     */
    Optional<ScaStatus> getAuthorisationScaStatus(String paymentId, String authorisationId, CmsAuthorisationType authorisationType);

    /**
     * Get information about PSU list by payment identifier
     *
     * @param paymentId String representation of the payment identifier
     * @return Response containing information about PSU
     */
    Optional<List<PsuIdData>> getPsuDataListByPaymentId(String paymentId);

    /**
     * Checks if requested authentication method is decoupled.
     *
     * @param authorisationId        String representation of the authorisation identifier
     * @param authenticationMethodId String representation of the available authentication method identifier
     * @return <code>true</code>, if authentication method is decoupled and <code>false</code> otherwise.
     */
    boolean isAuthenticationMethodDecoupled(String authorisationId, String authenticationMethodId);

    /**
     * Saves authentication methods in provided authorisation
     *
     * @param authorisationId String representation of the authorisation identifier
     * @param methods         List of authentication methods to be saved
     * @return <code>true</code> if authorisation was found and updated, <code>false</code> otherwise
     */
    boolean saveAuthenticationMethods(String authorisationId, List<CmsScaMethod> methods);

    /**
     * Updates pis sca approach
     *
     * @param authorisationId String representation of the authorisation identifier
     * @param scaApproach     chosen sca approach
     * @return <code>true</code> if authorisation was found and sca approach updated, <code>false</code> otherwise
     */
    boolean updateScaApproach(String authorisationId, ScaApproach scaApproach);

    /**
     * TODO add javadocs https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/722
     *
     * @return
     */
    Optional<AuthorisationScaApproachResponse> getAuthorisationScaApproach(String authorisationId, CmsAuthorisationType authorisationType);
}
