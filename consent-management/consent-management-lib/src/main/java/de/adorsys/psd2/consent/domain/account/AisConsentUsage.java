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

package de.adorsys.psd2.consent.domain.account;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;

@Data
@Entity(name = "ais_consent_usage")
@ApiModel(description = "Ais consent usage entity", value = "AisConsentUsage")
@NoArgsConstructor
public class AisConsentUsage {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ais_consent_usage_generator")
    @SequenceGenerator(name = "ais_consent_usage_generator", sequenceName = "ais_consent_usage_id_seq")
    private Long id;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "consent_id", nullable = false)
    private AisConsent consent;

    @Column(name = "date", nullable = false)
    @ApiModelProperty(value = "Date for the requested consent. The content is the local ASPSP date in ISODate Format", required = true, example = "2018-05-04")
    private LocalDate date;

    @Column(name = "usage", nullable = false)
    @ApiModelProperty(value = "Usage consent per one day.", required = true, example = "4")
    private int usage;

    public AisConsentUsage(AisConsent consent) {
        this.date = LocalDate.now();
        this.consent = consent;
        this.consent.getUsages().add(this);
    }

    public void increment() {
        this.usage++;
    }

    public void reset() {
        this.usage = 0;
    }
}
