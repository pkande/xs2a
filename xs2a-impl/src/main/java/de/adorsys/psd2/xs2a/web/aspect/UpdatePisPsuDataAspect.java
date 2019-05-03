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
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.Links;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aAuthenticationObject;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataRequest;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataResponse;
import de.adorsys.psd2.xs2a.service.ScaApproachResolver;
import de.adorsys.psd2.xs2a.service.message.MessageService;
import de.adorsys.psd2.xs2a.web.controller.PaymentController;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class UpdatePisPsuDataAspect extends AbstractLinkAspect<PaymentController> {
    private ScaApproachResolver scaApproachResolver;

    public UpdatePisPsuDataAspect(ScaApproachResolver scaApproachResolver, MessageService messageService, AspspProfileService aspspProfileService) {
        super(messageService, aspspProfileService);
        this.scaApproachResolver = scaApproachResolver;
    }

    @AfterReturning(pointcut = "execution(* de.adorsys.psd2.xs2a.service.PaymentAuthorisationService.updatePisCommonPaymentPsuData(..)) && args( request)", returning = "result", argNames = "result,request")
    public ResponseObject<Xs2aUpdatePisCommonPaymentPsuDataResponse> updatePisAuthorizationAspect(ResponseObject<Xs2aUpdatePisCommonPaymentPsuDataResponse> result, Xs2aUpdatePisCommonPaymentPsuDataRequest request) {
        if (!result.hasError()) {
            Xs2aUpdatePisCommonPaymentPsuDataResponse body = result.getBody();
            Links links = buildLink(request);
            ScaStatus scaStatus = body.getScaStatus();

            if (isScaStatusMethodAuthenticated(scaStatus)) {
                links.setSelectAuthenticationMethod(buildAuthorisationLink(request));

                // TODO https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/722
            } else if (isScaStatusMethodSelected(body.getChosenScaMethod(), scaStatus) && isEmbeddedScaApproach(request.getAuthorisationId())) {
                links.setAuthoriseTransaction(buildAuthorisationLink(request));
            } else if (isScaStatusFinalised(scaStatus)) {

                links.setScaStatus(buildAuthorisationLink(request));
            } else if (isScaStatusMethodIdentified(scaStatus)) {
                links.setUpdatePsuAuthentication(buildAuthorisationLink(request));
            }

            body.setLinks(links);
            return result;
        }

        return enrichErrorTextMessage(result);
    }

    private Links buildLink(Xs2aUpdatePisCommonPaymentPsuDataRequest request) {
        Links links = new Links();
        links.setSelf(buildPath(UrlHolder.PAYMENT_LINK_URL, request.getPaymentService(), request.getPaymentProduct(), request.getPaymentId()));
        links.setStatus(buildPath(UrlHolder.PAYMENT_STATUS_URL, request.getPaymentService(), request.getPaymentProduct(), request.getPaymentId()));
        return links;
    }

    private String buildAuthorisationLink(Xs2aUpdatePisCommonPaymentPsuDataRequest request) {
        return buildPath(UrlHolder.PIS_AUTHORISATION_LINK_URL, request.getPaymentService(), request.getPaymentProduct(),
                         request.getPaymentId(), request.getAuthorisationId());
    }

    private boolean isEmbeddedScaApproach(String authorisationId) {
        return scaApproachResolver.getInitiationScaApproach(authorisationId) == ScaApproach.EMBEDDED;
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
