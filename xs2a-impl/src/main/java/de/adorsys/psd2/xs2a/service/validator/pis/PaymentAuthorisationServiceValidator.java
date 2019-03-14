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
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.domain.pis.PaymentAuthorisationType;
import de.adorsys.psd2.xs2a.service.validator.PisEndpointAccessCheckerService;
import de.adorsys.psd2.xs2a.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.service.validator.tpp.PisTppInfoValidator;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import static de.adorsys.psd2.xs2a.domain.MessageErrorCode.RESOURCE_EXPIRED_403;
import static de.adorsys.psd2.xs2a.domain.MessageErrorCode.SERVICE_BLOCKED;
import static de.adorsys.psd2.xs2a.domain.TppMessageInformation.of;
import static de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType.PIS_403;

/**
 * Validator to be used for validating parameters, passed to the methods of
 * {@link de.adorsys.psd2.xs2a.service.PaymentAuthorisationService}
 */
@Component
@RequiredArgsConstructor
public class PaymentAuthorisationServiceValidator {
    private final PisTppInfoValidator pisTppInfoValidator;
    private final PisEndpointAccessCheckerService pisEndpointAccessCheckerService;

    /**
     * Validates payment used in
     * {@link de.adorsys.psd2.xs2a.service.PaymentAuthorisationService#createPisAuthorization}
     * by checking whether:
     * <ul>
     * <li>current TPP is valid for the payment</li>
     * <li>payment is not expired</li>
     * </ul>
     *
     * @param pisCommonPaymentResponse payment object, associated with paymentId passed to the method
     * @return valid result if the payment is valid, invalid result with appropriate error otherwise
     */
    public ValidationResult validateCreatePisAuthorisation(@NotNull PisCommonPaymentResponse pisCommonPaymentResponse) {
        ValidationResult tppValidationResult = pisTppInfoValidator.validateTpp(pisCommonPaymentResponse.getTppInfo());
        if (tppValidationResult.isNotValid()) {
            return tppValidationResult;
        }

        // TODO temporary solution: CMS should be refactored to return response objects instead of Strings, Enums, Booleans etc., so we should receive this error from CMS https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/581
        if (pisCommonPaymentResponse.getTransactionStatus() == TransactionStatus.RJCT) {
            return ValidationResult.invalid(PIS_403, of(RESOURCE_EXPIRED_403));
        }

        return ValidationResult.valid();
    }

    /**
     * Validates payment used in
     * {@link de.adorsys.psd2.xs2a.service.PaymentAuthorisationService#updatePisCommonPaymentPsuData}
     * by checking whether:
     * <ul>
     * <li>current TPP is valid for the payment</li>
     * <li>payment is not expired</li>
     * </ul>
     *
     * @param pisCommonPaymentResponse payment object, associated with paymentId passed to the method
     * @return valid result if the payment is valid, invalid result with appropriate error otherwise
     */
    public ValidationResult validateUpdatePisCommonPaymentPsuData(@NotNull PisCommonPaymentResponse pisCommonPaymentResponse, String authorisationId) {
        ValidationResult tppValidationResult = pisTppInfoValidator.validateTpp(pisCommonPaymentResponse.getTppInfo());
        if (tppValidationResult.isNotValid()) {
            return tppValidationResult;
        }

        if (!pisEndpointAccessCheckerService.isEndpointAccessible(authorisationId, PaymentAuthorisationType.INITIATION)) {
            return ValidationResult.invalid(PIS_403, of(SERVICE_BLOCKED));
        }

        // TODO temporary solution: CMS should be refactored to return response objects instead of Strings, Enums, Booleans etc., so we should receive this error from CMS https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/581
        if (pisCommonPaymentResponse.getTransactionStatus() == TransactionStatus.RJCT) {
            return ValidationResult.invalid(PIS_403, of(RESOURCE_EXPIRED_403));
        }

        return ValidationResult.valid();
    }

    /**
     * Validates payment used in
     * {@link de.adorsys.psd2.xs2a.service.PaymentAuthorisationService#getPaymentInitiationAuthorisations}
     * by checking whether:
     * <ul>
     * <li>current TPP is valid for the payment</li>
     * </ul>
     *
     * @param pisCommonPaymentResponse payment object, associated with paymentId passed to the method
     * @return valid result if the payment is valid, invalid result with appropriate error otherwise
     */
    public ValidationResult validateGetPaymentInitiationAuthorisations(@NotNull PisCommonPaymentResponse pisCommonPaymentResponse) {
        ValidationResult tppValidationResult = pisTppInfoValidator.validateTpp(pisCommonPaymentResponse.getTppInfo());
        if (tppValidationResult.isNotValid()) {
            return tppValidationResult;
        }

        return ValidationResult.valid();
    }

    /**
     * Validates payment used in
     * {@link de.adorsys.psd2.xs2a.service.PaymentAuthorisationService#getPaymentInitiationAuthorisationScaStatus}
     * by checking whether:
     * <ul>
     * <li>current TPP is valid for the payment</li>
     * </ul>
     *
     * @param pisCommonPaymentResponse payment object, associated with paymentId passed to the method
     * @return valid result if the payment is valid, invalid result with appropriate error otherwise
     */
    public ValidationResult validateGetPaymentInitiationAuthorisationScaStatus(@NotNull PisCommonPaymentResponse pisCommonPaymentResponse) {
        ValidationResult tppValidationResult = pisTppInfoValidator.validateTpp(pisCommonPaymentResponse.getTppInfo());
        if (tppValidationResult.isNotValid()) {
            return tppValidationResult;
        }

        return ValidationResult.valid();
    }
}
