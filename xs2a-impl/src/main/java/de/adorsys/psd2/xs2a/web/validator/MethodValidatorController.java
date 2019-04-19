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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class MethodValidatorController {
    private ErrorBuildingService errorBuildingService;
    private ObjectMapper objectMapper;

    private Map<String, MethodValidator> methodValidatorContext = new HashMap<>();

    @Autowired
    public MethodValidatorController(ErrorBuildingService errorBuildingService, ObjectMapper objectMapper) {
        this.errorBuildingService = errorBuildingService;
        this.objectMapper = objectMapper;
        createMethodValidationContext();
    }

    public Optional<MethodValidator> getMethod(String methodName) {
        return Optional.ofNullable(methodValidatorContext.get(methodName));
    }

    private void createMethodValidationContext() {
        methodValidatorContext.put("_createConsent", new ConsentMethodValidatorImpl(errorBuildingService, objectMapper));
        methodValidatorContext.put("_initiatePayment", new PaymentMethodValidatorImpl(errorBuildingService, objectMapper));
    }
}
