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

package de.adorsys.psd2.xs2a.integration;


import de.adorsys.aspsp.xs2a.spi.ASPSPXs2aApplication;
import de.adorsys.psd2.aspsp.profile.service.AspspProfileService;
import de.adorsys.psd2.consent.api.CmsAuthorisationType;
import de.adorsys.psd2.consent.api.pis.authorisation.CreatePisAuthorisationResponse;
import de.adorsys.psd2.consent.api.service.EventServiceEncrypted;
import de.adorsys.psd2.consent.api.service.PisCommonPaymentServiceEncrypted;
import de.adorsys.psd2.consent.api.service.TppStopListService;
import de.adorsys.psd2.xs2a.config.*;
import de.adorsys.psd2.xs2a.core.event.Event;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.AuthorisationScaApproachResponse;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.integration.builder.AspspSettingsBuilder;
import de.adorsys.psd2.xs2a.integration.builder.TppInfoBuilder;
import de.adorsys.psd2.xs2a.integration.builder.UrlBuilder;
import de.adorsys.psd2.xs2a.integration.builder.payment.PisCommonPaymentResponseBuilder;
import de.adorsys.psd2.xs2a.service.TppService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles({"integration-test", "mockspi"})
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(
    classes = ASPSPXs2aApplication.class)
@ContextConfiguration(classes = {
    CorsConfigurationProperties.class,
    ObjectMapperConfig.class,
    WebConfig.class,
    Xs2aEndpointPathConstant.class,
    Xs2aInterfaceConfig.class
})
public class PaymentControllerTest {
    private static final PaymentType SINGLE_PAYMENT_TYPE = PaymentType.SINGLE;
    private static final ScaStatus SCA_STATUS = ScaStatus.RECEIVED;
    private static final String SEPA_PAYMENT_PRODUCT = "sepa-credit-transfers";
    private static final String ENCRYPT_PAYMENT_ID = "DfLtDOgo1tTK6WQlHlb-TMPL2pkxRlhZ4feMa5F4tOWwNN45XLNAVfWwoZUKlQwb_=_bS6p6XvTWI";
    private static final String AUTHORISATION_ID = "e8356ea7-8e3e-474f-b5ea-2b89346cb2dc";
    private static final String CANCELLATION_ID = "cancellationId";
    private static final String HREF = "href";
    private static final TppInfo TPP_INFO = TppInfoBuilder.buildTppInfo();
    private static final ScaApproach SCA_APPROACH = ScaApproach.REDIRECT;
    private HttpHeaders httpHeadersExplicit = new HttpHeaders();

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AspspProfileService aspspProfileService;
    @MockBean
    private TppService tppService;
    @MockBean
    private TppStopListService tppStopListService;
    @MockBean
    private EventServiceEncrypted eventServiceEncrypted;
    @MockBean
    private PisCommonPaymentServiceEncrypted pisCommonPaymentServiceEncrypted;

    @Before
    public void init() {
        // common actions for all tests
        given(aspspProfileService.getScaApproaches()).willReturn(Collections.singletonList(SCA_APPROACH));
        given(aspspProfileService.getAspspSettings())
            .willReturn(AspspSettingsBuilder.buildAspspSettings());
        given(tppService.getTppInfo())
            .willReturn(TPP_INFO);
        given(tppService.getTppId())
            .willReturn(TPP_INFO.getAuthorisationNumber());
        given(tppStopListService.checkIfTppBlocked(TppInfoBuilder.buildTppUniqueParamsHolder()))
            .willReturn(false);
        given(eventServiceEncrypted.recordEvent(any(Event.class)))
            .willReturn(true);

        given(pisCommonPaymentServiceEncrypted.getPsuDataListByPaymentId(any()))
            .willReturn(Optional.of(Collections.singletonList(getPsuIdData())));

        given(pisCommonPaymentServiceEncrypted.createAuthorizationCancellation(any(), any()))
            .willReturn(Optional.of(new CreatePisAuthorisationResponse(CANCELLATION_ID, SCA_STATUS)));

        httpHeadersExplicit.add("Content-Type", "application/json");
        httpHeadersExplicit.add("tpp-qwac-certificate", "qwac certificate");
        httpHeadersExplicit.add("X-Request-ID", "2f77a125-aa7a-45c0-b414-cea25a116035");
        httpHeadersExplicit.add("PSU-ID", "PSU-123");
        httpHeadersExplicit.add("PSU-ID-Type", "Some type");
        httpHeadersExplicit.add("PSU-Corporate-ID", "Some corporate id");
        httpHeadersExplicit.add("PSU-Corporate-ID-Type", "Some corporate id type");
        httpHeadersExplicit.add("PSU-IP-Address", "1.1.1.1");

        // when we use Explicit auth mode we need to set 'true' and value 'signingBasketSupported' in profile also should be 'true'
        httpHeadersExplicit.add("TPP-Explicit-Authorisation-Preferred", "true");
    }

    @Test
    public void getPaymentInitiationScaStatus_successful() throws Exception {
        // Given
        given(pisCommonPaymentServiceEncrypted.getAuthorisationScaStatus(ENCRYPT_PAYMENT_ID, AUTHORISATION_ID, CmsAuthorisationType.CREATED))
            .willReturn(Optional.of(ScaStatus.RECEIVED));
        given(pisCommonPaymentServiceEncrypted.getCommonPaymentById(ENCRYPT_PAYMENT_ID))
            .willReturn(Optional.of(PisCommonPaymentResponseBuilder.buildPisCommonPaymentResponse()));
        given(pisCommonPaymentServiceEncrypted.getAuthorisationScaApproach(AUTHORISATION_ID, CmsAuthorisationType.CREATED))
            .willReturn(Optional.of(new AuthorisationScaApproachResponse(SCA_APPROACH)));

        MockHttpServletRequestBuilder requestBuilder = get(UrlBuilder.buildGetPaymentInitiationScaStatusUrl(SINGLE_PAYMENT_TYPE.getValue(), SEPA_PAYMENT_PRODUCT, ENCRYPT_PAYMENT_ID, AUTHORISATION_ID));
        requestBuilder.headers(httpHeadersExplicit);

        // When
        ResultActions resultActions = mockMvc.perform(requestBuilder);

        //Then
        resultActions.andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(content().json("{\"scaStatus\":\"received\"}"));
    }

    @Test
    public void cancelPaymentAuthorisation_successful() throws Exception {
        // Given
        given(pisCommonPaymentServiceEncrypted.getAuthorisationScaStatus(ENCRYPT_PAYMENT_ID, AUTHORISATION_ID, CmsAuthorisationType.CREATED))
            .willReturn(Optional.of(ScaStatus.RECEIVED));
        given(pisCommonPaymentServiceEncrypted.getCommonPaymentById(ENCRYPT_PAYMENT_ID))
            .willReturn(Optional.of(PisCommonPaymentResponseBuilder.buildPisCommonPaymentResponse()));
        given(pisCommonPaymentServiceEncrypted.getAuthorisationScaApproach(CANCELLATION_ID, CmsAuthorisationType.CANCELLED))
            .willReturn(Optional.of(new AuthorisationScaApproachResponse(SCA_APPROACH)));

        MockHttpServletRequestBuilder requestBuilder = post(UrlBuilder.buildGetPaymentCancellationAuthorisationUrl(SINGLE_PAYMENT_TYPE.getValue(), SEPA_PAYMENT_PRODUCT, ENCRYPT_PAYMENT_ID));
        requestBuilder.headers(httpHeadersExplicit);

        // When
        ResultActions resultActions = mockMvc.perform(requestBuilder);

        //Then
        resultActions.andExpect(status().isCreated())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
            .andExpect(content().json("{\"scaStatus\":\"received\"}"))
            .andExpect(content().json("{\"_links\":{\"scaStatus\":{" + HREF + ":\"http://localhost/v1/payments/" + SEPA_PAYMENT_PRODUCT + "/" + ENCRYPT_PAYMENT_ID + "/cancellation-authorisations/" + CANCELLATION_ID + "\"}}}"));
    }

    private PsuIdData getPsuIdData() {
        return new PsuIdData("PSU-123", "Some type", "Some corporate id", "Some corporate id type");
    }
}
