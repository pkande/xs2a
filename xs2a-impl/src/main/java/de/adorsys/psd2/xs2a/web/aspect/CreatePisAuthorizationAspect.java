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
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aCreatePisAuthorisationRequest;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aCreatePisAuthorisationResponse;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataResponse;
import de.adorsys.psd2.xs2a.service.ScaApproachResolver;
import de.adorsys.psd2.xs2a.service.message.MessageService;
import de.adorsys.psd2.xs2a.web.RedirectLinkBuilder;
import de.adorsys.psd2.xs2a.web.controller.PaymentController;
import de.adorsys.psd2.xs2a.web.link.CreatePisAuthorizationLinks;
import de.adorsys.psd2.xs2a.web.link.UpdatePisAuthorizationLinks;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

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

                response.setLinks(new CreatePisAuthorizationLinks(getHttpUrl(), scaApproachResolver,redirectLinkBuilder, createRequest,  response.getAuthorisationId()));
            } else if (result.getBody() instanceof Xs2aUpdatePisCommonPaymentPsuDataResponse) {
                Xs2aUpdatePisCommonPaymentPsuDataResponse response = (Xs2aUpdatePisCommonPaymentPsuDataResponse) result.getBody();
                response.setLinks(new UpdatePisAuthorizationLinks(getHttpUrl(), scaApproachResolver, response, createRequest));
            }

            return result;
        }
        return enrichErrorTextMessage(result);
    }

}
