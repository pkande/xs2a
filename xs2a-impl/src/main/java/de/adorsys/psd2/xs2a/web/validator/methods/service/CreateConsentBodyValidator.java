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

package de.adorsys.psd2.xs2a.web.validator.methods.service;

import de.adorsys.psd2.model.AccountAccess;
import de.adorsys.psd2.model.AccountReference;
import de.adorsys.psd2.model.Consents;
import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.web.validator.ErrorBuildingService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.CreditCardValidator;
import org.apache.commons.validator.routines.IBANValidator;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Currency;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CreateConsentBodyValidator {

    private final ErrorBuildingService errorBuildingService;

    public void validateConsentBody(Consents consents, MessageError messageError) {

        if (Objects.isNull(consents.getAccess())) {
            errorBuildingService.enrichMessageError(messageError, "Value 'access' should not be null");
        } else {
            validateAccountAccess(consents.getAccess(), messageError);
        }

        if (Objects.isNull(consents.getRecurringIndicator())) {
            errorBuildingService.enrichMessageError(messageError, "Value 'recurringIndicator' should not be null");
        }

        if (Objects.isNull(consents.getValidUntil())) {
            errorBuildingService.enrichMessageError(messageError, "Value 'validUntil' should not be null");
        } else {
            validateValidUntil(consents.getValidUntil(), messageError);
        }

        if (Objects.isNull(consents.getFrequencyPerDay())) {
            errorBuildingService.enrichMessageError(messageError, "Value 'frequencyPerDay' should not be null");
        } else {
            validateFrequencyPerDay(consents.getFrequencyPerDay(), messageError);
        }
    }

    private void validateAccountAccess(AccountAccess accountAccess, MessageError messageError) {
        List<AccountReference> references = accountAccess.getAccounts();
        references.forEach(ar -> validateAccountReference(ar, messageError));
    }

    private void validateAccountReference(AccountReference accountReference, MessageError messageError) {
        if (StringUtils.isNotBlank(accountReference.getIban()) && !isValidIban(accountReference.getIban())) {
            errorBuildingService.enrichMessageError(messageError, "Invalid IBAN format");
        }
        if (StringUtils.isNotBlank(accountReference.getBban()) && !isValidBban(accountReference.getBban())) {
            errorBuildingService.enrichMessageError(messageError, "Invalid BBAN format");
        }
        if (StringUtils.isNotBlank(accountReference.getPan()) && !isValidPan(accountReference.getPan())) {
            errorBuildingService.enrichMessageError(messageError, "Invalid PAN format");
        }
        if (StringUtils.isNotBlank(accountReference.getMaskedPan()) && !isValidMaskedPan(accountReference.getMaskedPan())) {
            errorBuildingService.enrichMessageError(messageError, "Masked PAN should not be more than 35 symbols");
        }
        if (StringUtils.isNotBlank(accountReference.getMsisdn()) && !isValidMsisdn(accountReference.getMsisdn())) {
            errorBuildingService.enrichMessageError(messageError, "MSISDN should not be more than 35 symbols");
        }
        if (StringUtils.isNotBlank(accountReference.getCurrency()) && !isValidCurrency(accountReference.getCurrency())) {
            errorBuildingService.enrichMessageError(messageError, "Invalid currency code format");
        }
    }

    private void validateValidUntil(LocalDate validUntil, MessageError messageError) {
        try {
            LocalDate.parse(String.valueOf(validUntil));
        } catch (DateTimeParseException e) {
            errorBuildingService.enrichMessageError(messageError, "Wrong 'validUntil' date value");
            return;
        }

        if (validUntil.isBefore(LocalDate.now())) {
            errorBuildingService.enrichMessageError(messageError, "Value 'validUntil' may not be in the past");
        }
    }

    private void validateFrequencyPerDay(Integer frequencyPerDay, MessageError messageError) {
        if (frequencyPerDay < 1) {
            errorBuildingService.enrichMessageError(messageError, "Value 'frequencyPerDay' should not be lower than 1");
        }
    }

    private boolean isValidIban(String iban) {
        IBANValidator validator = IBANValidator.getInstance();
        return validator.isValid(normalizeString(iban));
    }

    private boolean isValidBban(String bban) {
        return normalizeString(bban).length() >= 11
                   && normalizeString(bban).length() <= 28; // Can be extended with aprox 50 country specific masks
    }

    private boolean isValidPan(String pan) {
        CreditCardValidator validator = CreditCardValidator.genericCreditCardValidator(); //Can be extended with specification of credit card types (VISA, MasterCard, AMEX etc. with array in aspsp profile)
        return validator.isValid(normalizeString(pan));
    }

    private boolean isValidMaskedPan(String maskedPan) {
        return maskedPan.length() <= 35;
    }

    private boolean isValidMsisdn(String msisdn) {
        return msisdn.length() <= 35;
    }

    private boolean isValidCurrency(String currency) {
        try {
            Currency.getInstance(currency);
        } catch (IllegalArgumentException e) {
            return false;
        }
        return true;
    }

    private String normalizeString(String string) {
        return string.replaceAll("[^a-zA-Z0-9]", "");
    }
}
