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

package de.adorsys.psd2.xs2a.web.link;

import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.domain.Links;
import de.adorsys.psd2.xs2a.domain.consent.CreateConsentResponse;
import de.adorsys.psd2.xs2a.service.ScaApproachResolver;
import de.adorsys.psd2.xs2a.util.reader.JsonReader;
import de.adorsys.psd2.xs2a.web.RedirectLinkBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CreateConsentLinksTest {
    private static final String HTTP_URL = "http://url";
    private static final String CONSENT_ID = "9mp1PaotpXSToNCi";
    private static final String AUTHORISATION_ID = "463318a0-1e33-45d8-8209-e16444b18dda";
    private static final String BUILT_LINK = "built_redirect_link";

    @Mock
    private ScaApproachResolver scaApproachResolver;
    @Mock
    private RedirectLinkBuilder redirectLinkBuilder;

    private PsuIdData psuIdData;
    private CreateConsentLinks links;
    private CreateConsentResponse response;

    private Links expectedLinks;
    private JsonReader jsonReader;

    @Before
    public void setUp() {
        jsonReader = new JsonReader();
        psuIdData = jsonReader.getObjectFromFile("json/link/empty.json", PsuIdData.class);
        expectedLinks = new Links();

        response = new CreateConsentResponse(null, CONSENT_ID, null, null, null, null, true);
        response.setAuthorizationId(AUTHORISATION_ID);
    }

    @Test
    public void isScaStatusMethodAuthenticatedAndEmbeddedScaApproachAndExplicitMethodAndPsuDataIsEmptyAndMultiLevelRequired() {
        when(scaApproachResolver.getInitiationScaApproach(eq(AUTHORISATION_ID))).thenReturn(ScaApproach.EMBEDDED);

        links = new CreateConsentLinks(HTTP_URL, scaApproachResolver, response, redirectLinkBuilder, true, psuIdData);

        expectedLinks.setSelf("http://url/v1/consents/9mp1PaotpXSToNCi");
        expectedLinks.setStatus("http://url/v1/consents/9mp1PaotpXSToNCi/status");
        expectedLinks.setStartAuthorisationWithPsuIdentification("http://url/v1/consents/9mp1PaotpXSToNCi/authorisations");
        assertEquals(expectedLinks, links);
    }

    @Test
    public void isScaStatusMethodAuthenticatedAndEmbeddedScaApproachAndExplicitMethodAndPsuDataIsNotEmptyAndMultiLevelRequired() {
        psuIdData = jsonReader.getObjectFromFile("json/link/psu-id-data.json", PsuIdData.class);

        when(scaApproachResolver.getInitiationScaApproach(eq(AUTHORISATION_ID))).thenReturn(ScaApproach.EMBEDDED);

        links = new CreateConsentLinks(HTTP_URL, scaApproachResolver, response, redirectLinkBuilder, true, psuIdData);

        expectedLinks.setSelf("http://url/v1/consents/9mp1PaotpXSToNCi");
        expectedLinks.setStatus("http://url/v1/consents/9mp1PaotpXSToNCi/status");
        expectedLinks.setStartAuthorisationWithPsuAuthentication("http://url/v1/consents/9mp1PaotpXSToNCi/authorisations");
        assertEquals(expectedLinks, links);
    }

    @Test
    public void isScaStatusMethodAuthenticatedAndEmbeddedScaApproachAndExplicitMethodAndMultiLevelNotRequired() {
        response = new CreateConsentResponse(null, CONSENT_ID, null, null, null, null, false);
        response.setAuthorizationId(AUTHORISATION_ID);

        when(scaApproachResolver.getInitiationScaApproach(eq(AUTHORISATION_ID))).thenReturn(ScaApproach.EMBEDDED);

        links = new CreateConsentLinks(HTTP_URL, scaApproachResolver, response, redirectLinkBuilder, true, psuIdData);

        expectedLinks.setSelf("http://url/v1/consents/9mp1PaotpXSToNCi");
        expectedLinks.setStatus("http://url/v1/consents/9mp1PaotpXSToNCi/status");
        expectedLinks.setStartAuthorisation("http://url/v1/consents/9mp1PaotpXSToNCi/authorisations");
        assertEquals(expectedLinks, links);
    }

    @Test
    public void isScaStatusMethodAuthenticatedAndEmbeddedScaApproachAndImplicitMethodAndPsuDataIsEmpty() {
        when(scaApproachResolver.getInitiationScaApproach(eq(AUTHORISATION_ID))).thenReturn(ScaApproach.EMBEDDED);

        links = new CreateConsentLinks(HTTP_URL, scaApproachResolver, response, redirectLinkBuilder, false, psuIdData);

        expectedLinks.setSelf("http://url/v1/consents/9mp1PaotpXSToNCi");
        expectedLinks.setStatus("http://url/v1/consents/9mp1PaotpXSToNCi/status");
        expectedLinks.setScaStatus("http://url/v1/consents/9mp1PaotpXSToNCi/authorisations/463318a0-1e33-45d8-8209-e16444b18dda");
        expectedLinks.setUpdatePsuIdentification("http://url/v1/consents/9mp1PaotpXSToNCi/authorisations/463318a0-1e33-45d8-8209-e16444b18dda");
        assertEquals(expectedLinks, links);
    }


    @Test
    public void isScaStatusMethodAuthenticatedAndEmbeddedScaApproachAndImplicitMethodAndPsuDataIsNotEmpty() {
        psuIdData = jsonReader.getObjectFromFile("json/link/psu-id-data.json", PsuIdData.class);

        when(scaApproachResolver.getInitiationScaApproach(eq(AUTHORISATION_ID))).thenReturn(ScaApproach.EMBEDDED);

        links = new CreateConsentLinks(HTTP_URL, scaApproachResolver, response, redirectLinkBuilder, false, psuIdData);

        expectedLinks.setSelf("http://url/v1/consents/9mp1PaotpXSToNCi");
        expectedLinks.setStatus("http://url/v1/consents/9mp1PaotpXSToNCi/status");
        expectedLinks.setScaStatus("http://url/v1/consents/9mp1PaotpXSToNCi/authorisations/463318a0-1e33-45d8-8209-e16444b18dda");
        expectedLinks.setUpdatePsuAuthentication("http://url/v1/consents/9mp1PaotpXSToNCi/authorisations/463318a0-1e33-45d8-8209-e16444b18dda");
        assertEquals(expectedLinks, links);
    }

//    ------------------

    @Test
    public void isScaStatusMethodAuthenticatedAndDecoupledScaApproachAndExplicitMethodAndPsuDataIsEmptyAndMultiLevelRequired() {
        when(scaApproachResolver.getInitiationScaApproach(eq(AUTHORISATION_ID))).thenReturn(ScaApproach.DECOUPLED);

        links = new CreateConsentLinks(HTTP_URL, scaApproachResolver, response, redirectLinkBuilder, true, psuIdData);

        expectedLinks.setSelf("http://url/v1/consents/9mp1PaotpXSToNCi");
        expectedLinks.setStatus("http://url/v1/consents/9mp1PaotpXSToNCi/status");
        expectedLinks.setStartAuthorisationWithPsuIdentification("http://url/v1/consents/9mp1PaotpXSToNCi/authorisations");
        assertEquals(expectedLinks, links);
    }

    @Test
    public void isScaStatusMethodAuthenticatedAndDecoupledScaApproachAndExplicitMethodAndPsuDataIsNotEmptyAndMultiLevelRequired() {
        psuIdData = jsonReader.getObjectFromFile("json/link/psu-id-data.json", PsuIdData.class);

        when(scaApproachResolver.getInitiationScaApproach(eq(AUTHORISATION_ID))).thenReturn(ScaApproach.DECOUPLED);

        links = new CreateConsentLinks(HTTP_URL, scaApproachResolver, response, redirectLinkBuilder, true, psuIdData);

        expectedLinks.setSelf("http://url/v1/consents/9mp1PaotpXSToNCi");
        expectedLinks.setStatus("http://url/v1/consents/9mp1PaotpXSToNCi/status");
        expectedLinks.setStartAuthorisationWithPsuAuthentication("http://url/v1/consents/9mp1PaotpXSToNCi/authorisations");
        assertEquals(expectedLinks, links);
    }

    @Test
    public void isScaStatusMethodAuthenticatedAndDecoupledScaApproachAndExplicitMethodAndMultiLevelNotRequired() {
        response = new CreateConsentResponse(null, CONSENT_ID, null, null, null, null, false);
        response.setAuthorizationId(AUTHORISATION_ID);

        when(scaApproachResolver.getInitiationScaApproach(eq(AUTHORISATION_ID))).thenReturn(ScaApproach.DECOUPLED);

        links = new CreateConsentLinks(HTTP_URL, scaApproachResolver, response, redirectLinkBuilder, true, psuIdData);

        expectedLinks.setSelf("http://url/v1/consents/9mp1PaotpXSToNCi");
        expectedLinks.setStatus("http://url/v1/consents/9mp1PaotpXSToNCi/status");
        expectedLinks.setStartAuthorisation("http://url/v1/consents/9mp1PaotpXSToNCi/authorisations");
        assertEquals(expectedLinks, links);
    }

    @Test
    public void isScaStatusMethodAuthenticatedAndDecoupledScaApproachAndImplicitMethodAndPsuDataIsEmpty() {
        when(scaApproachResolver.getInitiationScaApproach(eq(AUTHORISATION_ID))).thenReturn(ScaApproach.DECOUPLED);

        links = new CreateConsentLinks(HTTP_URL, scaApproachResolver, response, redirectLinkBuilder, false, psuIdData);

        expectedLinks.setSelf("http://url/v1/consents/9mp1PaotpXSToNCi");
        expectedLinks.setStatus("http://url/v1/consents/9mp1PaotpXSToNCi/status");
        expectedLinks.setScaStatus("http://url/v1/consents/9mp1PaotpXSToNCi/authorisations/463318a0-1e33-45d8-8209-e16444b18dda");
        expectedLinks.setUpdatePsuIdentification("http://url/v1/consents/9mp1PaotpXSToNCi/authorisations/463318a0-1e33-45d8-8209-e16444b18dda");
        assertEquals(expectedLinks, links);
    }


    @Test
    public void isScaStatusMethodAuthenticatedAndDecoupledScaApproachAndImplicitMethodAndPsuDataIsNotEmpty() {
        psuIdData = jsonReader.getObjectFromFile("json/link/psu-id-data.json", PsuIdData.class);

        when(scaApproachResolver.getInitiationScaApproach(eq(AUTHORISATION_ID))).thenReturn(ScaApproach.DECOUPLED);

        links = new CreateConsentLinks(HTTP_URL, scaApproachResolver, response, redirectLinkBuilder, false, psuIdData);

        expectedLinks.setSelf("http://url/v1/consents/9mp1PaotpXSToNCi");
        expectedLinks.setStatus("http://url/v1/consents/9mp1PaotpXSToNCi/status");
        expectedLinks.setScaStatus("http://url/v1/consents/9mp1PaotpXSToNCi/authorisations/463318a0-1e33-45d8-8209-e16444b18dda");
        expectedLinks.setUpdatePsuAuthentication("http://url/v1/consents/9mp1PaotpXSToNCi/authorisations/463318a0-1e33-45d8-8209-e16444b18dda");
        assertEquals(expectedLinks, links);
    }

    @Test
    public void scaApproachRedirectAndExplicitMethod() {
        when(scaApproachResolver.getInitiationScaApproach(eq(AUTHORISATION_ID))).thenReturn(ScaApproach.REDIRECT);

        links = new CreateConsentLinks(HTTP_URL, scaApproachResolver, response, redirectLinkBuilder, true, psuIdData);

        expectedLinks.setSelf("http://url/v1/consents/9mp1PaotpXSToNCi");
        expectedLinks.setStatus("http://url/v1/consents/9mp1PaotpXSToNCi/status");
        expectedLinks.setStartAuthorisation("http://url/v1/consents/9mp1PaotpXSToNCi/authorisations");
        assertEquals(expectedLinks, links);
    }

    @Test
    public void scaApproachRedirectAndImplicitMethod() {
        when(scaApproachResolver.getInitiationScaApproach(eq(AUTHORISATION_ID))).thenReturn(ScaApproach.REDIRECT);
        when(redirectLinkBuilder.buildConsentScaRedirectLink(eq(CONSENT_ID), eq(AUTHORISATION_ID))).thenReturn(BUILT_LINK);

        links = new CreateConsentLinks(HTTP_URL, scaApproachResolver, response, redirectLinkBuilder, false, psuIdData);

        expectedLinks.setSelf("http://url/v1/consents/9mp1PaotpXSToNCi");
        expectedLinks.setStatus("http://url/v1/consents/9mp1PaotpXSToNCi/status");
        expectedLinks.setScaRedirect(BUILT_LINK);
        expectedLinks.setScaStatus("http://url/v1/consents/9mp1PaotpXSToNCi/authorisations/463318a0-1e33-45d8-8209-e16444b18dda");
        assertEquals(expectedLinks, links);
    }

    @Test
    public void scaApproachOAuth() {
        response.setAuthorizationId(null);

        when(scaApproachResolver.resolveScaApproach()).thenReturn(ScaApproach.OAUTH);

        links = new CreateConsentLinks(HTTP_URL, scaApproachResolver, response, redirectLinkBuilder, false, psuIdData);

        expectedLinks.setSelf("http://url/v1/consents/9mp1PaotpXSToNCi");
        expectedLinks.setStatus("http://url/v1/consents/9mp1PaotpXSToNCi/status");
        assertEquals(expectedLinks, links);
    }
}
