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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class PaymentTypeValidatorContext {

    private Map<String, PaymentValidator> context = new HashMap<>();
    private List<PaymentValidator> paymentValidators;

    @Autowired
    public PaymentTypeValidatorContext(List<PaymentValidator> paymentValidators) {
        this.paymentValidators = paymentValidators;
        createContext();
    }

    public Optional<PaymentValidator> getValidator(String paymentType) {
        return Optional.ofNullable(context.get(paymentType));
    }

    private void createContext() {
        paymentValidators.forEach(m -> context.put(m.getPaymentType().name(), m));
    }
}
