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
import de.adorsys.psd2.xs2a.component.MultiReadHttpServletRequest;
import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.web.validator.ErrorBuildingService;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public class AbstractBodyValidatorImpl {

    protected ErrorBuildingService errorBuildingService;
    protected ObjectMapper objectMapper;

    AbstractBodyValidatorImpl(ErrorBuildingService errorBuildingService, ObjectMapper objectMapper) {
        this.errorBuildingService = errorBuildingService;
        this.objectMapper = objectMapper;
    }

    protected Object mapBodyToPaymentObject(HttpServletRequest request, MessageError messageError) {

        Object body = null;

        MultiReadHttpServletRequest multiReadRequest = new MultiReadHttpServletRequest(request);
        try {
            body = objectMapper.readValue(multiReadRequest.getInputStream(), Object.class);
        } catch (IOException e) {
            errorBuildingService.enrichMessageError(messageError, "Cannot deserialize the request body");
        }

        return body;
    }

}
