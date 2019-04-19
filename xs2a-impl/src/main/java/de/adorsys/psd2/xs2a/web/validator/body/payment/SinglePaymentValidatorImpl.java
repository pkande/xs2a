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
import de.adorsys.psd2.model.PaymentInitiationJson;
import de.adorsys.psd2.xs2a.domain.code.Xs2aPurposeCode;
import de.adorsys.psd2.xs2a.domain.pis.Remittance;
import de.adorsys.psd2.xs2a.domain.pis.SinglePayment;
import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.web.validator.ErrorBuildingService;
import de.adorsys.psd2.xs2a.web.validator.body.payment.mapper.PaymentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class SinglePaymentValidatorImpl implements PaymentValidator{

    private ErrorBuildingService errorBuildingService;
    private PaymentMapper paymentMapper;

    @Autowired
    public SinglePaymentValidatorImpl(ErrorBuildingService errorBuildingService, PaymentMapper paymentMapper) {
        this.errorBuildingService = errorBuildingService;
        this.paymentMapper = paymentMapper;
    }

    @Override
    public void validate(Object body, MessageError messageError) {
        doValidation(paymentMapper.mapToXs2aSinglePayment(convertPayment(body, PaymentInitiationJson.class)), messageError);
    }

    private void doValidation(SinglePayment singlePayment, MessageError messageError) {
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

}
