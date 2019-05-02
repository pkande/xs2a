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

package de.adorsys.psd2.xs2a.service;

import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.sca.AuthorisationScaApproachResponse;
import de.adorsys.psd2.xs2a.domain.ScaApproachHolder;
import de.adorsys.psd2.xs2a.domain.pis.PaymentAuthorisationType;
import de.adorsys.psd2.xs2a.service.authorization.pis.PisAuthorisationService;
import de.adorsys.psd2.xs2a.service.consent.Xs2aAisConsentService;
import de.adorsys.psd2.xs2a.service.discovery.ServiceTypeDiscoveryService;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ServiceType;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static de.adorsys.psd2.xs2a.core.profile.ScaApproach.DECOUPLED;

@Service
@RequiredArgsConstructor
public class ScaApproachResolver {
    private final ServiceTypeDiscoveryService serviceTypeDiscoveryService;
    private final Xs2aAisConsentService xs2aAisConsentService;
    private final PisAuthorisationService pisAuthorisationService;
    private final ScaApproachHolder scaApproachHolder;

    @NotNull
    public ScaApproach resolveScaApproach(@NotNull String authorisationId, PaymentAuthorisationType authorisationType) {
        // TODO check if needed https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/722
        if (scaApproachHolder.isNotEmpty()) {
            return scaApproachHolder.getScaApproach();
        }

        Optional<AuthorisationScaApproachResponse> scaApproachResponse = Optional.empty();
        ServiceType serviceType = serviceTypeDiscoveryService.getServiceType();
        if (serviceType == ServiceType.AIS) {
            scaApproachResponse = xs2aAisConsentService.getAuthorisationScaApproach(authorisationId);
        } else if (serviceType == ServiceType.PIS) {
            scaApproachResponse = pisAuthorisationService.getAuthorisationScaApproach(authorisationId, authorisationType);
        }

        if (!scaApproachResponse.isPresent()) {
            // TODO write error message https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/722
            throw new IllegalArgumentException();
        }

        return scaApproachResponse.get().getScaApproach();
    }

    /**
     * Forcefully sets current SCA approach to <code>DECOUPLED</code>.
     * Should ONLY be used for switching from Embedded to Decoupled approach during SCA method selection
     */
    public void forceDecoupledScaApproach() {
        scaApproachHolder.setScaApproach(DECOUPLED);
    }
}
