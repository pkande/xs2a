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

package de.adorsys.psd2.xs2a.service.validator.pis;

import de.adorsys.psd2.consent.api.pis.proto.PisCommonPaymentResponse;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.domain.pis.PaymentAuthorisationType;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
import de.adorsys.psd2.xs2a.service.validator.PisEndpointAccessCheckerService;
import de.adorsys.psd2.xs2a.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.service.validator.tpp.PisTppInfoValidator;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.List;

import static de.adorsys.psd2.xs2a.domain.MessageErrorCode.PSU_CREDENTIALS_INVALID;
import static de.adorsys.psd2.xs2a.domain.MessageErrorCode.SERVICE_BLOCKED;

@Component
@RequiredArgsConstructor
public class PaymentCancellationAuthorisationServiceValidator {
    private final PisTppInfoValidator pisTppInfoValidator;
    private final PisEndpointAccessCheckerService pisEndpointAccessCheckerService;

    public ValidationResult validateCreatePisCancellationAuthorisation(@NotNull PisCommonPaymentResponse pisCommonPaymentResponse, @NotNull PsuIdData psuData) {
        ValidationResult tppValidationResult = pisTppInfoValidator.validateTpp(pisCommonPaymentResponse);
        if (tppValidationResult.isNotValid()) {
            return tppValidationResult;
        }

        if (psuData.isNotEmpty() && !isPsuDataCorrect(pisCommonPaymentResponse, psuData)) {
            return ValidationResult.invalid(ErrorType.PIS_401, TppMessageInformation.of(PSU_CREDENTIALS_INVALID));
        }

        return ValidationResult.valid();
    }

    public ValidationResult validateUpdatePisCancellationPsuData(@NotNull PisCommonPaymentResponse pisCommonPaymentResponse, String authorisationId) {
        ValidationResult tppValidationResult = pisTppInfoValidator.validateTpp(pisCommonPaymentResponse);
        if (tppValidationResult.isNotValid()) {
            return tppValidationResult;
        }

        if (!pisEndpointAccessCheckerService.isEndpointAccessible(authorisationId, PaymentAuthorisationType.CANCELLATION)) {
            return ValidationResult.invalid(ErrorType.PIS_403, TppMessageInformation.of(SERVICE_BLOCKED));
        }

        return ValidationResult.valid();
    }

    public ValidationResult validateGetPaymentInitiationCancellationAuthorisationInformation(@NotNull PisCommonPaymentResponse pisCommonPaymentResponse) {
        ValidationResult tppValidationResult = pisTppInfoValidator.validateTpp(pisCommonPaymentResponse);
        if (tppValidationResult.isNotValid()) {
            return tppValidationResult;
        }

        return ValidationResult.valid();
    }

    public ValidationResult validateGetPaymentCancellationAuthorisationScaStatus(@NotNull PisCommonPaymentResponse pisCommonPaymentResponse) {
        ValidationResult tppValidationResult = pisTppInfoValidator.validateTpp(pisCommonPaymentResponse);
        if (tppValidationResult.isNotValid()) {
            return tppValidationResult;
        }

        return ValidationResult.valid();
    }

    private boolean isPsuDataCorrect(PisCommonPaymentResponse pisCommonPaymentResponse, PsuIdData psuData) {
        List<PsuIdData> psuIdDataList = pisCommonPaymentResponse.getPsuData();

        return psuIdDataList.stream()
                   .anyMatch(psu -> psu.contentEquals(psuData));
    }
}
