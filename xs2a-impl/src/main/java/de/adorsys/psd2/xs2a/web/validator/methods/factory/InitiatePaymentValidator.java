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

package de.adorsys.psd2.xs2a.web.validator.methods.factory;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.adorsys.psd2.xs2a.component.MultiReadHttpServletRequest;
import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.web.validator.ErrorBuildingService;
import de.adorsys.psd2.xs2a.web.validator.methods.MethodHeadersValidator;
import de.adorsys.psd2.xs2a.web.validator.methods.service.InitiatePaymentBodyValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Map;

@RequiredArgsConstructor
@Service("_initiatePayment")
public class InitiatePaymentValidator implements MethodHeadersValidator {

    private final ObjectMapper objectMapper;
    private final InitiatePaymentBodyValidator initiatePaymentBodyValidator;
    private final ErrorBuildingService errorBuildingService;

    // Headers that should be validated here:
    // TODO: enrich the list:
    // PSU-ID, PSU-ID-Type, ....
    // https://wiki.adorsys.de/display/PSD2/VR-+002+PIS+Fields+validation

    @Override
    public void validate(HttpServletRequest request, MessageError messageError) {

        validatePsuId(request, messageError);
        validatePsuIdType(request, messageError);
        // TODO:

        Map<String, String> pathParametersMap = (Map<String, String>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        Object body = mapBodyToPaymentObject(request, messageError);

        // TODO: think how to validate body (maybe interface and different implementations with Object in signature)
        initiatePaymentBodyValidator.validateInitiatePaymentBody(body, pathParametersMap, messageError);
    }

    private Object mapBodyToPaymentObject(HttpServletRequest request, MessageError messageError) {

        Object body = new Object();

        MultiReadHttpServletRequest multiReadRequest = new MultiReadHttpServletRequest(request);
        try {
            body = objectMapper.readValue(multiReadRequest.getInputStream(), Object.class);
        } catch (IOException e) {
            errorBuildingService.enrichMessageError(messageError, "Cannot deserialize the request body");
        }

        return body;
    }



    private void validatePsuId(HttpServletRequest request, MessageError messageError) {
        // TODO:
    }

    private void validatePsuIdType(HttpServletRequest request, MessageError messageError) {
        // TODO:
    }
}
