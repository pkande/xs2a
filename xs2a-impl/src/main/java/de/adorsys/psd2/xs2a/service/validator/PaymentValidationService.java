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

package de.adorsys.psd2.xs2a.service.validator;

import de.adorsys.psd2.xs2a.domain.MessageErrorCode;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.pis.BulkPayment;
import de.adorsys.psd2.xs2a.domain.pis.PeriodicPayment;
import de.adorsys.psd2.xs2a.domain.pis.SinglePayment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

import static de.adorsys.psd2.xs2a.domain.MessageErrorCode.PERIOD_INVALID;
import static de.adorsys.psd2.xs2a.domain.TppMessageInformation.of;
import static de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType.PIS_400;

@Service
@RequiredArgsConstructor
public class PaymentValidationService {

    public ResponseObject validateSinglePayment(SinglePayment singePayment) {

        return isDateInThePast(singePayment.getRequestedExecutionDate())
                   ? buildErrorResponse(PERIOD_INVALID)
                   : ResponseObject.builder().build();
    }

    public ResponseObject validatePeriodicPayment(PeriodicPayment periodicPayment) {

        return areDatesInvalidInPeriodicPayment(periodicPayment)
                   ? buildErrorResponse(PERIOD_INVALID)
                   : ResponseObject.builder().build();
    }

    public ResponseObject validateBulkPayment(BulkPayment bulkPayment) {

        return isDateInThePast(bulkPayment.getRequestedExecutionDate())
                   ? buildErrorResponse(PERIOD_INVALID)
                   : ResponseObject.builder().build();
    }

    private boolean areDatesInvalidInPeriodicPayment(PeriodicPayment periodicPayment) {
        LocalDate paymentStartDate = periodicPayment.getStartDate();
        LocalDate paymentEndDate = periodicPayment.getEndDate();

        return isDateInThePast(paymentStartDate)
                   || Optional.ofNullable(paymentEndDate)
                          .map(dt -> dt.isBefore(paymentStartDate))
                          .orElse(false);
    }

    private boolean isDateInThePast(LocalDate dateToCheck) {
        return Optional.ofNullable(dateToCheck)
                   .map(date -> date.isBefore(LocalDate.now()))
                   .orElse(false);
    }

    private ResponseObject buildErrorResponse(MessageErrorCode errorCode) {
        return ResponseObject.builder()
                   .fail(PIS_400, of(errorCode))
                   .build();
    }

}
