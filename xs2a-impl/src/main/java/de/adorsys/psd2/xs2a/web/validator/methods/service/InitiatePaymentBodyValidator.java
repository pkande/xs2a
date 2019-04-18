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

import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.domain.Xs2aAmount;
import de.adorsys.psd2.xs2a.domain.pis.BulkPayment;
import de.adorsys.psd2.xs2a.domain.pis.PeriodicPayment;
import de.adorsys.psd2.xs2a.domain.pis.SinglePayment;
import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.web.validator.ErrorBuildingService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.CreditCardValidator;
import org.apache.commons.validator.routines.IBANValidator;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;

import static de.adorsys.psd2.xs2a.core.profile.PaymentType.PERIODIC;
import static de.adorsys.psd2.xs2a.core.profile.PaymentType.SINGLE;

@Service
@RequiredArgsConstructor
public class InitiatePaymentBodyValidator {

    private final ErrorBuildingService errorBuildingService;

    public void validateInitiatePaymentBody(Object body, Map<String, String> pathParametersMap, MessageError messageError) {
        String paymentService = pathParametersMap.get("payment-service");

        PaymentType paymentType = PaymentType.getByValue(paymentService).orElseThrow(() -> new IllegalArgumentException("Unsupported payment service"));

        if (paymentType == SINGLE) {
            validateSinglePayment((SinglePayment) body, messageError);
        } else if (paymentType == PERIODIC) {
            validatePeriodicPayment((PeriodicPayment) body, messageError);
        } else {
            validateBulkPayment((BulkPayment) body, messageError);
        }
    }

    private void validateSinglePayment(SinglePayment singlePayment, MessageError messageError) {
        if (Objects.nonNull(singlePayment.getEndToEndIdentification()) &&
                singlePayment.getEndToEndIdentification().length() > 35) {
            errorBuildingService.enrichMessageError(messageError, "Value 'endToEndIdentification' should not be more than 35 symbols");
        }

        if (Objects.isNull(singlePayment.getDebtorAccount())) {
            errorBuildingService.enrichMessageError(messageError, "Value 'debtorAccount' should not be null");
        } else {
            validateAccount(singlePayment.getDebtorAccount(), messageError);
        }

        if (Objects.isNull(singlePayment.getInstructedAmount())) {
            errorBuildingService.enrichMessageError(messageError, "Value 'instructedAmount' should not be null");
        } else {
            validateInstructedAmount(singlePayment.getInstructedAmount(), messageError);
        }

        if (Objects.isNull(singlePayment.getCreditorAccount())) {
            errorBuildingService.enrichMessageError(messageError, "Value 'creditorAccount' should not be null");
        } else {
            validateAccount(singlePayment.getCreditorAccount(), messageError);
        }

    }

    private void validateInstructedAmount(Xs2aAmount instructedAmount, MessageError messageError) {
        if (Objects.isNull(instructedAmount.getCurrency())) {
            errorBuildingService.enrichMessageError(messageError, "Value 'currency' should not be null");
        }
        if (Objects.isNull(instructedAmount.getAmount())) {
            errorBuildingService.enrichMessageError(messageError, "Value 'amount' should not be null");
        } else {
            if (instructedAmount.getAmount().length() > 140) {
                errorBuildingService.enrichMessageError(messageError, "Value 'instructedAmount' should not be more than 140 symbols");
            }
        }
    }

    private void validateAccount(AccountReference debtorAccount, MessageError messageError) {
        if (StringUtils.isNotBlank(debtorAccount.getIban()) && !isValidIban(debtorAccount.getIban())) {
            errorBuildingService.enrichMessageError(messageError, "Invalid IBAN format");
        }
        if (StringUtils.isNotBlank(debtorAccount.getBban()) && !isValidBban(debtorAccount.getBban())) {
            errorBuildingService.enrichMessageError(messageError, "Invalid BBAN format");
        }
        if (StringUtils.isNotBlank(debtorAccount.getPan()) && !isValidPan(debtorAccount.getPan())) {
            errorBuildingService.enrichMessageError(messageError, "Invalid PAN format");
        }
        if (StringUtils.isNotBlank(debtorAccount.getMaskedPan()) && !isValidMaskedPan(debtorAccount.getMaskedPan())) {
            errorBuildingService.enrichMessageError(messageError, "Masked PAN should not be more than 35 symbols");
        }
        if (StringUtils.isNotBlank(debtorAccount.getMsisdn()) && !isValidMsisdn(debtorAccount.getMsisdn())) {
            errorBuildingService.enrichMessageError(messageError, "MSISDN should not be more than 35 symbols");
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

    private String normalizeString(String string) {
        return string.replaceAll("[^a-zA-Z0-9]", "");
    }


    private void validatePeriodicPayment(PeriodicPayment periodicPayment, MessageError messageError) {

    }

    private void validateBulkPayment(BulkPayment bulkPayment, MessageError messageError) {

    }


}
