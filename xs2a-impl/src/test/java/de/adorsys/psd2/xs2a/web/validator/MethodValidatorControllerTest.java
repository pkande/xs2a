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

import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.*;

public class MethodValidatorControllerTest {

    @Test
    public void get() {
        Optional<MethodValidatorController> methodValidator = MethodValidatorController.get("_createConsent");
        assertTrue(methodValidator.isPresent());
        assertEquals(MethodValidatorController.CREATE_CONSENT, methodValidator.get());

        methodValidator = MethodValidatorController.get("_initiatePayment");
        assertTrue(methodValidator.isPresent());
        assertEquals(MethodValidatorController.INITIATE_PAYMENT, methodValidator.get());

        methodValidator = MethodValidatorController.get("");
        assertFalse(methodValidator.isPresent());

        methodValidator = MethodValidatorController.get(null);
        assertFalse(methodValidator.isPresent());

        methodValidator = MethodValidatorController.get("unknown method");
        assertFalse(methodValidator.isPresent());
    }
}
