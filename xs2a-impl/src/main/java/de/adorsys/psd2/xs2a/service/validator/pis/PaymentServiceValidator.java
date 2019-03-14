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

@Component
@RequiredArgsConstructor
public class PaymentServiceValidator {
    private static final String MESSAGE_ERROR_NO_PSU = "Please provide the PSU identification data";

    private final AspspProfileServiceWrapper aspspProfileServiceWrapper;
    private final PisTppInfoValidator pisTppInfoValidator;
    private final GetCommonPaymentByIdResponseValidator getCommonPaymentByIdResponseValidator;

    public ValidationResult validateCreatePayment(@NotNull PaymentInitiationParameters paymentInitiationParameters) {
        if (aspspProfileServiceWrapper.isPsuInInitialRequestMandated()
                && paymentInitiationParameters.getPsuData().isEmpty()) {
            return ValidationResult.invalid(PIS_400, TppMessageInformation.of(FORMAT_ERROR, MESSAGE_ERROR_NO_PSU));
        }

        return ValidationResult.valid();
    }

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
