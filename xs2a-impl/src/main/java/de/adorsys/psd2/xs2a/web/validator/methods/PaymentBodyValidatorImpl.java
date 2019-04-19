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

package de.adorsys.psd2.xs2a.web.validator.methods;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.adorsys.psd2.model.*;
import de.adorsys.psd2.xs2a.core.pis.PisDayOfExecution;
import de.adorsys.psd2.xs2a.core.pis.PisExecutionRule;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.domain.Xs2aAmount;
import de.adorsys.psd2.xs2a.domain.address.Xs2aAddress;
import de.adorsys.psd2.xs2a.domain.address.Xs2aCountryCode;
import de.adorsys.psd2.xs2a.domain.code.Xs2aFrequencyCode;
import de.adorsys.psd2.xs2a.domain.code.Xs2aPurposeCode;
import de.adorsys.psd2.xs2a.domain.pis.BulkPayment;
import de.adorsys.psd2.xs2a.domain.pis.PeriodicPayment;
import de.adorsys.psd2.xs2a.domain.pis.Remittance;
import de.adorsys.psd2.xs2a.domain.pis.SinglePayment;
import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.web.validator.ErrorBuildingService;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.CreditCardValidator;
import org.apache.commons.validator.routines.IBANValidator;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static de.adorsys.psd2.xs2a.core.profile.PaymentType.PERIODIC;
import static de.adorsys.psd2.xs2a.core.profile.PaymentType.SINGLE;

public class PaymentBodyValidatorImpl extends AbstractBodyValidatorImpl {

    public PaymentBodyValidatorImpl(ErrorBuildingService errorBuildingService, ObjectMapper objectMapper) {
        super(errorBuildingService, objectMapper);
    }

    @Override
    public void validate(HttpServletRequest request, MessageError messageError) {

        Map<String, String> pathParametersMap = (Map<String, String>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        Object body = mapBodyToPaymentObject(request, messageError);

        // In case of wrong JSON - we don't proceed the inner fields validation.
        if (body == null) {
            return;
        }

        validateInitiatePaymentBody(body, pathParametersMap, messageError);
    }

    private <R> R convertPayment(Object payment, Class<R> clazz) {
        return objectMapper.convertValue(payment, clazz);
    }


    private void validateInitiatePaymentBody(Object body, Map<String, String> pathParametersMap, MessageError messageError) {
        String paymentService = pathParametersMap.get("payment-service");

        PaymentType paymentType = PaymentType.getByValue(paymentService).orElseThrow(() -> new IllegalArgumentException("Unsupported payment service"));

        if (paymentType == SINGLE) {
            validateSinglePayment(mapToXs2aSinglePayment(convertPayment(body, PaymentInitiationJson.class)), messageError);
        } else if (paymentType == PERIODIC) {
            validatePeriodicPayment(mapToXs2aPeriodicPayment(convertPayment(body, PeriodicPaymentInitiationJson.class)), messageError);
        } else {
            validateBulkPayment(mapToXs2aBulkPayment(convertPayment(body, BulkPaymentInitiationJson.class)), messageError);
        }
    }

    private void validateSinglePayment(SinglePayment singlePayment, MessageError messageError) {
        if (Objects.nonNull(singlePayment.getEndToEndIdentification())) {
            checkFieldForMaxLength(singlePayment.getEndToEndIdentification(), "endToEndIdentification", 35, messageError);
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

        if (Objects.isNull(singlePayment.getCreditorName())) {
            errorBuildingService.enrichMessageError(messageError, "Value 'creditorName' should not be null");
        } else {
            checkFieldForMaxLength(singlePayment.getCreditorName(), "creditorName", 70, messageError);
        }
    }

    private void validatePeriodicPayment(PeriodicPayment periodicPayment, MessageError messageError) {
        if (Objects.nonNull(periodicPayment.getEndToEndIdentification())) {
            checkFieldForMaxLength(periodicPayment.getEndToEndIdentification(), "endToEndIdentification", 35, messageError);
        }

        if (Objects.isNull(periodicPayment.getDebtorAccount())) {
            errorBuildingService.enrichMessageError(messageError, "Value 'debtorAccount' should not be null");
        } else {
            validateAccount(periodicPayment.getDebtorAccount(), messageError);
        }

        if (Objects.isNull(periodicPayment.getInstructedAmount())) {
            errorBuildingService.enrichMessageError(messageError, "Value 'instructedAmount' should not be null");
        } else {
            validateInstructedAmount(periodicPayment.getInstructedAmount(), messageError);
        }

        if (Objects.isNull(periodicPayment.getCreditorAccount())) {
            errorBuildingService.enrichMessageError(messageError, "Value 'creditorAccount' should not be null");
        } else {
            validateAccount(periodicPayment.getCreditorAccount(), messageError);
        }

        if (Objects.isNull(periodicPayment.getCreditorName())) {
            errorBuildingService.enrichMessageError(messageError, "Value 'creditorName' should not be null");
        } else {
            checkFieldForMaxLength(periodicPayment.getCreditorName(), "creditorName", 70, messageError);
        }
    }

    private void validateInstructedAmount(Xs2aAmount instructedAmount, MessageError messageError) {
        if (Objects.isNull(instructedAmount.getCurrency())) {
            errorBuildingService.enrichMessageError(messageError, "Value 'currency' should not be null");
        }
        if (Objects.isNull(instructedAmount.getAmount())) {
            errorBuildingService.enrichMessageError(messageError, "Value 'amount' should not be null");
        } else {
            checkFieldForMaxLength(instructedAmount.getAmount(), "amount", 140, messageError);
        }
    }

    private void validateAccount(AccountReference accountReference, MessageError messageError) {
        if (StringUtils.isNotBlank(accountReference.getIban()) && !isValidIban(accountReference.getIban())) {
            errorBuildingService.enrichMessageError(messageError, "Invalid IBAN format");
        }
        if (StringUtils.isNotBlank(accountReference.getBban()) && !isValidBban(accountReference.getBban())) {
            errorBuildingService.enrichMessageError(messageError, "Invalid BBAN format");
        }
        if (StringUtils.isNotBlank(accountReference.getPan())) {
            checkFieldForMaxLength(accountReference.getPan(), "PAN", 35, messageError);
        }
        if (StringUtils.isNotBlank(accountReference.getMaskedPan())) {
            checkFieldForMaxLength(accountReference.getMaskedPan(), "Masked PAN", 35, messageError);
        }
        if (StringUtils.isNotBlank(accountReference.getMsisdn())) {
            checkFieldForMaxLength(accountReference.getMsisdn(), "MSISDN", 35, messageError);
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

    private void validateBulkPayment(BulkPayment bulkPayment, MessageError messageError) {

    }

    private SinglePayment mapToXs2aSinglePayment(PaymentInitiationJson paymentRequest) {
        SinglePayment payment = new SinglePayment();

        payment.setEndToEndIdentification(paymentRequest.getEndToEndIdentification());
        payment.setDebtorAccount(mapToXs2aAccountReference(paymentRequest.getDebtorAccount()));
        payment.setUltimateDebtor("NOT SUPPORTED");
        payment.setInstructedAmount(mapToXs2aAmount(paymentRequest.getInstructedAmount()));
        payment.setCreditorAccount(mapToXs2aAccountReference(paymentRequest.getCreditorAccount()));
        payment.setCreditorAgent(paymentRequest.getCreditorAgent());
        payment.setCreditorName(paymentRequest.getCreditorName());
        payment.setCreditorAddress(mapToXs2aAddress(paymentRequest.getCreditorAddress()));
        payment.setUltimateCreditor(paymentRequest.getCreditorName());
        payment.setPurposeCode(new Xs2aPurposeCode("N/A"));
        payment.setRemittanceInformationUnstructured(paymentRequest.getRemittanceInformationUnstructured());
        payment.setRemittanceInformationStructured(new Remittance());
        payment.setRequestedExecutionDate(paymentRequest.getRequestedExecutionDate());
        return payment;
    }

    private AccountReference mapToXs2aAccountReference(Object reference12) {
        return objectMapper.convertValue(reference12, AccountReference.class);
    }

    public Xs2aAmount mapToXs2aAmount(Amount amount) {
        return Optional.ofNullable(amount)
                   .map(a -> {
                       Xs2aAmount amountTarget = new Xs2aAmount();
                       amountTarget.setAmount(a.getAmount());
                       amountTarget.setCurrency(getCurrencyByCode(a.getCurrency()));
                       return amountTarget;
                   })
                   .orElse(null);
    }

    private Currency getCurrencyByCode(String code) {
        return StringUtils.isNotBlank(code)
                   ? Currency.getInstance(code)
                   : null;
    }

    private Xs2aAddress mapToXs2aAddress(Address address) {
        return Optional.ofNullable(address)
                   .map(a -> {
                       Xs2aAddress targetAddress = new Xs2aAddress();
                       targetAddress.setStreet(a.getStreet());
                       targetAddress.setBuildingNumber(a.getBuildingNumber());
                       targetAddress.setCity(a.getCity());
                       targetAddress.setPostalCode(a.getPostalCode());
                       targetAddress.setCountry(new Xs2aCountryCode(a.getCountry()));
                       return targetAddress;
                   })
                   .orElseGet(Xs2aAddress::new);
    }

    private PeriodicPayment mapToXs2aPeriodicPayment(PeriodicPaymentInitiationJson paymentRequest) {
        PeriodicPayment payment = new PeriodicPayment();

        payment.setEndToEndIdentification(paymentRequest.getEndToEndIdentification());
        payment.setDebtorAccount(mapToXs2aAccountReference(paymentRequest.getDebtorAccount()));
        payment.setUltimateDebtor("NOT SUPPORTED");
        payment.setInstructedAmount(mapToXs2aAmount(paymentRequest.getInstructedAmount()));
        payment.setCreditorAccount(mapToXs2aAccountReference(paymentRequest.getCreditorAccount()));
        payment.setCreditorAgent(paymentRequest.getCreditorAgent());
        payment.setCreditorName(paymentRequest.getCreditorName());
        payment.setCreditorAddress(mapToXs2aAddress(paymentRequest.getCreditorAddress()));
        payment.setUltimateCreditor(paymentRequest.getCreditorName());
        payment.setPurposeCode(new Xs2aPurposeCode("N/A"));
        payment.setRemittanceInformationUnstructured(paymentRequest.getRemittanceInformationUnstructured());
        payment.setRemittanceInformationStructured(new Remittance());
        payment.setRequestedExecutionDate(LocalDate.now());
        payment.setRequestedExecutionTime(OffsetDateTime.now().plusHours(1));

        payment.setStartDate(paymentRequest.getStartDate());
        payment.setExecutionRule(mapToPisExecutionRule(paymentRequest.getExecutionRule()).orElse(null));
        payment.setEndDate(paymentRequest.getEndDate());
        payment.setFrequency(mapToXs2aFrequencyCode(paymentRequest.getFrequency()));
        payment.setDayOfExecution(mapToPisDayOfExecution(paymentRequest.getDayOfExecution()).orElse(null));
        return payment;
    }

    private Optional<PisExecutionRule> mapToPisExecutionRule(ExecutionRule rule) {
        return Optional.ofNullable(rule)
                   .map(ExecutionRule::toString)
                   .flatMap(PisExecutionRule::getByValue);
    }

    private Xs2aFrequencyCode mapToXs2aFrequencyCode(FrequencyCode frequency) {
        return Xs2aFrequencyCode.valueOf(frequency.name());
    }

    private Optional<PisDayOfExecution> mapToPisDayOfExecution(DayOfExecution dayOfExecution) {
        return Optional.ofNullable(dayOfExecution)
                   .map(DayOfExecution::toString)
                   .flatMap(PisDayOfExecution::getByValue);
    }

    private BulkPayment mapToXs2aBulkPayment(BulkPaymentInitiationJson paymentRequest) {
        BulkPayment bulkPayment = new BulkPayment();
        bulkPayment.setBatchBookingPreferred(paymentRequest.getBatchBookingPreferred());
        bulkPayment.setDebtorAccount(mapToXs2aAccountReference(paymentRequest.getDebtorAccount()));
        bulkPayment.setRequestedExecutionDate(paymentRequest.getRequestedExecutionDate());
        bulkPayment.setRequestedExecutionTime(paymentRequest.getRequestedExecutionTime());
        bulkPayment.setPayments(mapBulkPaymentToSinglePayments(paymentRequest));
        return bulkPayment;
    }

    private List<SinglePayment> mapBulkPaymentToSinglePayments(BulkPaymentInitiationJson paymentRequest) {
        return paymentRequest.getPayments().stream()
                   .map(p -> {
                       SinglePayment payment = new SinglePayment();
                       payment.setDebtorAccount(mapToXs2aAccountReference(paymentRequest.getDebtorAccount()));
                       payment.setRequestedExecutionDate(paymentRequest.getRequestedExecutionDate());
                       payment.setEndToEndIdentification(p.getEndToEndIdentification());
                       payment.setUltimateDebtor("NOT SUPPORTED");
                       payment.setInstructedAmount(mapToXs2aAmount(p.getInstructedAmount()));
                       payment.setCreditorAccount(mapToXs2aAccountReference(p.getCreditorAccount()));
                       payment.setCreditorAgent(p.getCreditorAgent());
                       payment.setCreditorName(p.getCreditorName());
                       payment.setCreditorAddress(mapToXs2aAddress(p.getCreditorAddress()));
                       payment.setUltimateCreditor(null);
                       payment.setPurposeCode(new Xs2aPurposeCode(null));
                       payment.setRemittanceInformationUnstructured(p.getRemittanceInformationUnstructured());
                       payment.setRemittanceInformationStructured(new Remittance());
                       payment.setRequestedExecutionTime(paymentRequest.getRequestedExecutionTime());
                       return payment;
                   })
                   .collect(Collectors.toList());
    }

}
