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

package de.adorsys.psd2.xs2a.web.validator;

import de.adorsys.psd2.xs2a.exception.MessageError;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Optional;

public enum MethodValidatorController {

    CREATE_CONSENT("_createConsent", new ConsentMethodValidatorImpl()),
    INITIATE_PAYMENT("_initiatePayment", new PaymentMethodValidatorImpl());

    private String value;
    private MethodValidator methodValidator;

    MethodValidatorController(String value, MethodValidator methodValidator) {
        this.value = value;
        this.methodValidator = methodValidator;
    }

    public String getValue() {
        return value;
    }

    public void validate(HttpServletRequest request, MessageError messageError) {
        methodValidator.validate(request, messageError);
    }

    public static Optional<MethodValidatorController> get(String methodName) {
        return Arrays.stream(values())
                   .filter(m -> m.getValue().equals(methodName))
                   .findFirst();
    }
}
