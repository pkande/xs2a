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

import de.adorsys.psd2.consent.domain.account.AisConsent;
import de.adorsys.psd2.consent.domain.account.AisConsentUsage;
import de.adorsys.psd2.consent.repository.AisConsentUsageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Service;

import javax.persistence.LockModeType;
import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AisConsentUsageService {
    private final AisConsentUsageRepository aisConsentUsageRepository;

    @Lock(value = LockModeType.WRITE)
    void incrementUsage(AisConsent aisConsent) {
        AisConsentUsage aisConsentUsage = getUsage(aisConsent);
        int usage = aisConsentUsage.getUsage();
        int newUsage = ++usage;
        aisConsentUsage.setUsage(newUsage);
        aisConsentUsageRepository.save(aisConsentUsage);
    }

    @Lock(value = LockModeType.WRITE)
    public void resetUsage(AisConsent aisConsent) {
        AisConsentUsage aisConsentUsage = getUsage(aisConsent);
        aisConsentUsage.setUsage(0);
        aisConsentUsageRepository.save(aisConsentUsage);
    }

    public int getUsageCounter(AisConsent aisConsent) {
        Integer usage = getCurrentAisConsentUsage(aisConsent)
                            .map(AisConsentUsage::getUsage)
                            .orElse(0);

        return Math.max(aisConsent.getAllowedFrequencyPerDay() - usage, 0);
    }

    private AisConsentUsage getUsage(AisConsent aisConsent) {
        return getCurrentAisConsentUsage(aisConsent)
                   .orElseGet(() -> new AisConsentUsage(aisConsent));
    }

    private Optional<AisConsentUsage> getCurrentAisConsentUsage(AisConsent aisConsent) {
        return aisConsentUsageRepository.findByConsentAndUsageDate(aisConsent, LocalDate.now());
    }
}
