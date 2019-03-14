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
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.domain.pis.PaymentInitiationParameters;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.service.validator.GetCommonPaymentByIdResponseValidator;
import de.adorsys.psd2.xs2a.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.service.validator.tpp.PisTppInfoValidator;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import static de.adorsys.psd2.xs2a.domain.MessageErrorCode.FORMAT_ERROR;
import static de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType.PIS_400;

/**
 * Validator to be used for validating parameters, passed to the methods of
 * {@link de.adorsys.psd2.xs2a.service.PaymentService}
 */
@Component
@RequiredArgsConstructor
public class PaymentServiceValidator {
    private static final String MESSAGE_ERROR_NO_PSU = "Please provide the PSU identification data";

    private final AspspProfileServiceWrapper aspspProfileServiceWrapper;
    private final PisTppInfoValidator pisTppInfoValidator;
    private final GetCommonPaymentByIdResponseValidator getCommonPaymentByIdResponseValidator;

    /**
     * Validates parameters of {@link de.adorsys.psd2.xs2a.service.PaymentService#createPayment}
     * by checking whether:
     * <ul>
     * <li>PSU Data is present in parameters if it's mandated</li>
     * </ul>
     *
     * @param paymentInitiationParameters payment initiation parameters passed to the method
     * @return valid result if the parameters are valid, invalid result with appropriate error otherwise
     */
    public ValidationResult validateCreatePayment(@NotNull PaymentInitiationParameters paymentInitiationParameters) {
        if (aspspProfileServiceWrapper.isPsuInInitialRequestMandated()
                && paymentInitiationParameters.getPsuData().isEmpty()) {
            return ValidationResult.invalid(PIS_400, TppMessageInformation.of(FORMAT_ERROR, MESSAGE_ERROR_NO_PSU));
        }

        return ValidationResult.valid();
    }

    /**
     * Validates payment used in {@link de.adorsys.psd2.xs2a.service.PaymentService#getPaymentById}
     * by checking whether:
     * <ul>
     * <li>current TPP is valid for the payment</li>
     * <li>given payment's type and product are valid for the payment</li>
     * </ul>
     *
     * @param pisCommonPaymentResponse payment object, associated with paymentId passed to the method
     * @param paymentType              payment type passed to the method
     * @param paymentProduct           payment product passed to the method
     * @return valid result if the payment is valid, invalid result with appropriate error otherwise
     */
    public ValidationResult validateGetPaymentById(@NotNull PisCommonPaymentResponse pisCommonPaymentResponse, PaymentType paymentType, String paymentProduct) {
        ValidationResult tppValidationResult = pisTppInfoValidator.validateTpp(pisCommonPaymentResponse.getTppInfo());
        if (tppValidationResult.isNotValid()) {
            return tppValidationResult;
        }

        ValidationResult getCommonPaymentValidationResult = getCommonPaymentByIdResponseValidator.validateRequest(pisCommonPaymentResponse, paymentType, paymentProduct);
        if (getCommonPaymentValidationResult.isNotValid()) {
            return getCommonPaymentValidationResult;
        }

        return ValidationResult.valid();
    }

    /**
     * Validates payment used in {@link de.adorsys.psd2.xs2a.service.PaymentService#getPaymentStatusById}
     * by checking whether:
     * <ul>
     * <li>current TPP is valid for the payment</li>
     * <li>given payment's type and product are valid for the payment</li>
     * </ul>
     *
     * @param pisCommonPaymentResponse payment object, associated with paymentId passed to the method
     * @param paymentType              payment type passed to the method
     * @param paymentProduct           payment product passed to the method
     * @return valid result if the payment is valid, invalid result with appropriate error otherwise
     */
    public ValidationResult validateGetPaymentStatusById(@NotNull PisCommonPaymentResponse pisCommonPaymentResponse, PaymentType paymentType, String paymentProduct) {
        ValidationResult tppValidationResult = pisTppInfoValidator.validateTpp(pisCommonPaymentResponse.getTppInfo());
        if (tppValidationResult.isNotValid()) {
            return tppValidationResult;
        }

        ValidationResult getCommonPaymentValidationResult = getCommonPaymentByIdResponseValidator.validateRequest(pisCommonPaymentResponse, paymentType, paymentProduct);
        if (getCommonPaymentValidationResult.isNotValid()) {
            return getCommonPaymentValidationResult;
        }

        return ValidationResult.valid();
    }

    /**
     * Validates payment used in {@link de.adorsys.psd2.xs2a.service.PaymentService#cancelPayment}
     * by checking whether:
     * <ul>
     * <li>current TPP is valid for the payment</li>
     * <li>given payment's type and product are valid for the payment</li>
     * </ul>
     *
     * @param pisCommonPaymentResponse payment object, associated with paymentId passed to the method
     * @param paymentType              payment type passed to the method
     * @param paymentProduct           payment product passed to the method
     * @return valid result if the payment is valid, invalid result with appropriate error otherwise
     */
    public ValidationResult validateCancelPayment(@NotNull PisCommonPaymentResponse pisCommonPaymentResponse, PaymentType paymentType, String paymentProduct) {
        ValidationResult tppValidationResult = pisTppInfoValidator.validateTpp(pisCommonPaymentResponse.getTppInfo());
        if (tppValidationResult.isNotValid()) {
            return tppValidationResult;
        }

        ValidationResult getCommonPaymentValidationResult = getCommonPaymentByIdResponseValidator.validateRequest(pisCommonPaymentResponse, paymentType, paymentProduct);
        if (getCommonPaymentValidationResult.isNotValid()) {
            return getCommonPaymentValidationResult;
        }

        return ValidationResult.valid();
    }
}
