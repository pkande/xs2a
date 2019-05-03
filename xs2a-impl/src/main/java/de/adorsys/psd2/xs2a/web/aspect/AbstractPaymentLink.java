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
import de.adorsys.psd2.xs2a.domain.Links;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.pis.PaymentInitiationParameters;
import de.adorsys.psd2.xs2a.domain.pis.PaymentInitiationResponse;
import de.adorsys.psd2.xs2a.service.InitialScaApproachResolver;
import de.adorsys.psd2.xs2a.service.authorization.AuthorisationMethodDecider;
import de.adorsys.psd2.xs2a.service.message.MessageService;
import de.adorsys.psd2.xs2a.web.RedirectLinkBuilder;

import java.util.EnumSet;

import static de.adorsys.psd2.xs2a.core.pis.TransactionStatus.RJCT;
import static de.adorsys.psd2.xs2a.core.profile.ScaApproach.*;

public abstract class AbstractPaymentLink<T> extends AbstractLinkAspect<T> {
    private InitialScaApproachResolver scaApproachResolver;
    private final AuthorisationMethodDecider authorisationMethodDecider;
    private final RedirectLinkBuilder redirectLinkBuilder;

    public AbstractPaymentLink(InitialScaApproachResolver scaApproachResolver, MessageService messageService, AuthorisationMethodDecider authorisationMethodDecider, RedirectLinkBuilder redirectLinkBuilder, AspspProfileService aspspProfileService) {
        super(messageService, aspspProfileService);
        this.scaApproachResolver = scaApproachResolver;
        this.authorisationMethodDecider = authorisationMethodDecider;
        this.redirectLinkBuilder = redirectLinkBuilder;
    }

    @SuppressWarnings("unchecked")
    protected ResponseObject<?> enrichLink(ResponseObject<?> result, PaymentInitiationParameters paymentRequestParameters) {
        Object body = result.getBody();
        doEnrichLink(paymentRequestParameters, (PaymentInitiationResponse) body);

        return result;
    }

    private void doEnrichLink(PaymentInitiationParameters paymentRequestParameters, PaymentInitiationResponse body) {
        body.setLinks(buildPaymentLinks(paymentRequestParameters, body));
    }

    private Links buildPaymentLinks(PaymentInitiationParameters paymentRequestParameters, PaymentInitiationResponse body) {
        if (RJCT == body.getTransactionStatus()) {
            return null;
        }
        String paymentService = paymentRequestParameters.getPaymentType().getValue();
        String paymentProduct = paymentRequestParameters.getPaymentProduct();
        String paymentId = body.getPaymentId();

        Links links = buildDefaultPaymentLinks(paymentService, paymentProduct, paymentId);

        if (EnumSet.of(EMBEDDED, DECOUPLED).contains(scaApproachResolver.resolveScaApproach())) {
            return addEmbeddedDecoupledRelatedLinks(links, paymentRequestParameters, body);
        } else if (scaApproachResolver.resolveScaApproach() == REDIRECT) {
            return addRedirectRelatedLinks(links, paymentRequestParameters, body);
        } else if (scaApproachResolver.resolveScaApproach() == OAUTH) {
            links.setScaOAuth("scaOAuth"); //TODO generate link for oauth https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/326
        }
        return links;
    }

    private Links addEmbeddedDecoupledRelatedLinks(Links links, PaymentInitiationParameters paymentRequestParameters, PaymentInitiationResponse body) {
        String paymentService = paymentRequestParameters.getPaymentType().getValue();
        String paymentProduct = paymentRequestParameters.getPaymentProduct();
        String paymentId = body.getPaymentId();
        String authorizationId = body.getAuthorizationId();

        if (authorisationMethodDecider.isExplicitMethod(paymentRequestParameters.isTppExplicitAuthorisationPreferred(), body.isMultilevelScaRequired())) {
            // TODO refactor isSigningBasketSupported https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/811
            boolean isSigningBasketSupported = !body.isMultilevelScaRequired();

            if (isSigningBasketSupported) { // no more data needs to be updated
                links.setStartAuthorisation(buildPath(UrlHolder.START_PIS_AUTHORISATION_URL, paymentService, paymentProduct, paymentId));

            } else if (paymentRequestParameters.getPsuData().isEmpty()) {
                links.setStartAuthorisationWithPsuIdentification(buildPath(UrlHolder.START_PIS_AUTHORISATION_URL, paymentService, paymentProduct, paymentId));
            } else {
                links.setStartAuthorisationWithPsuAuthentication(buildPath(UrlHolder.START_PIS_AUTHORISATION_URL, paymentService, paymentProduct, paymentId));
            }
        } else {
            links.setScaStatus(
                buildPath(UrlHolder.PIS_AUTHORISATION_LINK_URL, paymentService, paymentProduct, paymentId, authorizationId));
            if (paymentRequestParameters.getPsuData().isEmpty()) {
                links.setUpdatePsuIdentification(
                    buildPath(UrlHolder.PIS_AUTHORISATION_LINK_URL, paymentService, paymentProduct, paymentId, authorizationId));
            } else {
                links.setUpdatePsuAuthentication(
                    buildPath(UrlHolder.PIS_AUTHORISATION_LINK_URL, paymentService, paymentProduct, paymentId, authorizationId));
            }
        }

        return links;
    }

    private Links addRedirectRelatedLinks(Links links, PaymentInitiationParameters paymentRequestParameters, PaymentInitiationResponse body) {
        String paymentService = paymentRequestParameters.getPaymentType().getValue();
        String paymentProduct = paymentRequestParameters.getPaymentProduct();
        String paymentId = body.getPaymentId();
        String authorisationId = body.getAuthorizationId();

        if (authorisationMethodDecider.isExplicitMethod(paymentRequestParameters.isTppExplicitAuthorisationPreferred(), body.isMultilevelScaRequired())) {
            links.setStartAuthorisation(buildPath(UrlHolder.START_PIS_AUTHORISATION_URL, paymentService, paymentProduct, paymentId));
        } else {
            String scaRedirectLink = redirectLinkBuilder.buildPaymentScaRedirectLink(body.getPaymentId(), authorisationId);
            links.setScaRedirect(scaRedirectLink);
            links.setScaStatus(
                buildPath(UrlHolder.PIS_AUTHORISATION_LINK_URL, paymentService, paymentProduct, paymentId, authorisationId));
        }
        return links;
    }
}
