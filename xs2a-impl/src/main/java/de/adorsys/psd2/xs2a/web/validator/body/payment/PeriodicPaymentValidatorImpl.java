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

package de.adorsys.psd2.xs2a.web.validator.body.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.domain.pis.PeriodicPayment;
import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.web.validator.ErrorBuildingService;
import de.adorsys.psd2.xs2a.web.validator.body.payment.mapper.PaymentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Objects;

@Component
public class PeriodicPaymentValidatorImpl extends SinglePaymentValidatorImpl {

    private ErrorBuildingService errorBuildingService;
    private PaymentMapper paymentMapper;

    @Autowired
    public PeriodicPaymentValidatorImpl(ErrorBuildingService errorBuildingService, ObjectMapper objectMapper,
                                        PaymentMapper paymentMapper) {
        super(errorBuildingService, objectMapper, paymentMapper);
        this.errorBuildingService = errorBuildingService;
        this.paymentMapper = paymentMapper;
    }

    @Override
    public PaymentType getPaymentType() {
        return PaymentType.PERIODIC;
    }

    @Override
    public void validate(Object body, MessageError messageError) {
        doValidation(paymentMapper.getPeriodicPayment(body), messageError);
    }

    void doValidation(PeriodicPayment periodicPayment, MessageError messageError) {
        super.doValidation(periodicPayment, messageError);

        if (Objects.isNull(periodicPayment.getStartDate())) {
            errorBuildingService.enrichMessageError(messageError, "Value 'startDate' should not be null");
        } else {
            validateStartDate(periodicPayment.getStartDate(), messageError);
        }

        if (Objects.nonNull(periodicPayment.getExecutionRule())) {
            checkFieldForMaxLength(periodicPayment.getExecutionRule().getValue(), "executionRule", 140, messageError);
        }

        if (Objects.isNull(periodicPayment.getFrequency())) {
            errorBuildingService.enrichMessageError(messageError, "Value 'frequency' should not be null");
        }
    }

    private void validateStartDate(LocalDate startDate, MessageError messageError) {
        if (startDate.isBefore(LocalDate.now())) {
            errorBuildingService.enrichMessageError(messageError, "Value 'startDate' should not be in the past");
        }
    }
}
