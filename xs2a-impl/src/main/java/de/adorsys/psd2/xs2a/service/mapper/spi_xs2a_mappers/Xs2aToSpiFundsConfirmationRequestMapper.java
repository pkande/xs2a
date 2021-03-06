/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
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

package de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers;

import de.adorsys.psd2.xs2a.domain.fund.FundsConfirmationRequest;
import de.adorsys.psd2.xs2a.spi.domain.fund.SpiFundsConfirmationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class Xs2aToSpiFundsConfirmationRequestMapper {
    private final Xs2aToSpiAccountReferenceMapper xs2aToSpiAccountReferenceMapper;
    private final Xs2aToSpiAmountMapper xs2aToSpiAmountMapper;

    public SpiFundsConfirmationRequest mapToSpiFundsConfirmationRequest(FundsConfirmationRequest request) {
        SpiFundsConfirmationRequest spiRequest = new SpiFundsConfirmationRequest();
        spiRequest.setPsuAccount(xs2aToSpiAccountReferenceMapper.mapToSpiAccountReference(request.getPsuAccount()));
        spiRequest.setInstructedAmount(xs2aToSpiAmountMapper.mapToSpiAmount(request.getInstructedAmount()));
        spiRequest.setCardNumber(request.getCardNumber());
        spiRequest.setPayee(request.getPayee());
        return spiRequest;
    }
}
