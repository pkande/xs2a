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

package de.adorsys.psd2.xs2a.web.aspect;

import de.adorsys.psd2.aspsp.profile.service.AspspProfileService;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.Links;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aAuthenticationObject;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aCreatePisAuthorisationRequest;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aCreatePisAuthorisationResponse;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataResponse;
import de.adorsys.psd2.xs2a.service.ScaApproachResolver;
import de.adorsys.psd2.xs2a.service.message.MessageService;
import de.adorsys.psd2.xs2a.web.RedirectLinkBuilder;
import de.adorsys.psd2.xs2a.web.controller.PaymentController;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.EnumSet;

import static de.adorsys.psd2.xs2a.core.profile.ScaApproach.*;

@Slf4j
@Aspect
@Component
public class CreatePisAuthorizationAspect extends AbstractLinkAspect<PaymentController> {
    private ScaApproachResolver scaApproachResolver;
    private RedirectLinkBuilder redirectLinkBuilder;

    public CreatePisAuthorizationAspect(ScaApproachResolver scaApproachResolver, MessageService messageService,
                                        AspspProfileService aspspProfileService, RedirectLinkBuilder redirectLinkBuilder) {
        super(messageService, aspspProfileService);
        this.scaApproachResolver = scaApproachResolver;
        this.redirectLinkBuilder = redirectLinkBuilder;
    }

    @AfterReturning(pointcut = "execution(* de.adorsys.psd2.xs2a.service.PaymentAuthorisationService.createPisAuthorisation(..)) && args(createRequest)", returning = "result", argNames = "result,createRequest")
    public ResponseObject createPisAuthorizationAspect(ResponseObject result, Xs2aCreatePisAuthorisationRequest createRequest) {
        if (!result.hasError()) {
            if (result.getBody() instanceof Xs2aCreatePisAuthorisationResponse) {
                Xs2aCreatePisAuthorisationResponse response = (Xs2aCreatePisAuthorisationResponse) result.getBody();

                response.setLinks(buildLink(createRequest, response.getAuthorisationId()));
            } else if (result.getBody() instanceof Xs2aUpdatePisCommonPaymentPsuDataResponse) {
                Xs2aUpdatePisCommonPaymentPsuDataResponse response = (Xs2aUpdatePisCommonPaymentPsuDataResponse) result.getBody();
                response.setLinks(updateLink(response, createRequest));
            }

            return result;
        }
        return enrichErrorTextMessage(result);
    }

    private Links buildLink(Xs2aCreatePisAuthorisationRequest createRequest, String authorisationId) {
        String paymentId = createRequest.getPaymentId();
        String paymentType = createRequest.getPaymentService();

        String paymentProduct = createRequest.getPaymentProduct();
        PsuIdData psuData = createRequest.getPsuData();

        Links links = buildDefaultPaymentLinks(paymentType, paymentProduct, paymentId);

        ScaApproach initiationScaApproach = scaApproachResolver.getInitiationScaApproach(authorisationId);
        if (EnumSet.of(EMBEDDED, DECOUPLED).contains(initiationScaApproach)) {
            String path = UrlHolder.PIS_AUTHORISATION_LINK_URL;
            if (psuData.isEmpty()) {
                links.setUpdatePsuIdentification(buildPath(path, paymentType, paymentProduct, paymentId, authorisationId));
            } else {
                links.setUpdatePsuAuthentication(buildPath(path, paymentType, paymentProduct, paymentId, authorisationId));
            }
        } else if (initiationScaApproach == REDIRECT) {
            String scaRedirectLink = redirectLinkBuilder.buildPaymentScaRedirectLink(paymentId, authorisationId);
            links.setScaRedirect(scaRedirectLink);
        }

        return links;
    }

    public Links updateLink(Xs2aUpdatePisCommonPaymentPsuDataResponse response, Xs2aCreatePisAuthorisationRequest createRequest) {
        Links links = buildDefaultPaymentLinks(createRequest.getPaymentService(), createRequest.getPaymentProduct(), response.getPaymentId());
        ScaStatus scaStatus = response.getScaStatus();

        if (isScaStatusMethodAuthenticated(scaStatus)) {
            links.setSelectAuthenticationMethod(buildAuthorisationLink(response, createRequest));

            // TODO https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/722
        } else if (isScaStatusMethodSelected(response.getChosenScaMethod(), scaStatus) &&
                                                                              scaApproachResolver.getInitiationScaApproach(response.getAuthorisationId()) == EMBEDDED) {
            links.setAuthoriseTransaction(buildAuthorisationLink(response, createRequest));
        } else if (isScaStatusFinalised(scaStatus)) {

            links.setScaStatus(buildAuthorisationLink(response, createRequest));
        } else if (isScaStatusMethodIdentified(scaStatus)) {
            links.setUpdatePsuAuthentication(buildAuthorisationLink(response, createRequest));
        }

        return links;
    }

    private String buildAuthorisationLink(Xs2aUpdatePisCommonPaymentPsuDataResponse response, Xs2aCreatePisAuthorisationRequest createRequest) {
        return buildPath(UrlHolder.PIS_AUTHORISATION_LINK_URL, createRequest.getPaymentService(),
                         createRequest.getPaymentProduct(), response.getPaymentId(), response.getAuthorisationId());
    }

    private boolean isScaStatusFinalised(ScaStatus scaStatus) {
        return scaStatus == ScaStatus.FINALISED;
    }

    private boolean isScaStatusMethodSelected(Xs2aAuthenticationObject chosenScaMethod, ScaStatus scaStatus) {
        return chosenScaMethod != null
                   && scaStatus == ScaStatus.SCAMETHODSELECTED;
    }

    private boolean isScaStatusMethodAuthenticated(ScaStatus scaStatus) {
        return scaStatus == ScaStatus.PSUAUTHENTICATED;
    }

    private boolean isScaStatusMethodIdentified(ScaStatus scaStatus) {
        return scaStatus == ScaStatus.PSUIDENTIFIED;
    }
}
