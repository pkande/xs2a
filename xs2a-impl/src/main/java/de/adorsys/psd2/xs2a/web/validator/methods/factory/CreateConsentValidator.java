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
import de.adorsys.psd2.model.Consents;
import de.adorsys.psd2.xs2a.component.MultiReadHttpServletRequest;
import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.web.validator.ErrorBuildingService;
import de.adorsys.psd2.xs2a.web.validator.methods.MethodHeadersValidator;
import de.adorsys.psd2.xs2a.web.validator.methods.service.CreateConsentBodyValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@RequiredArgsConstructor
@Service("_createConsent")
public class CreateConsentValidator implements MethodHeadersValidator {

    private final ObjectMapper objectMapper;
    private final CreateConsentBodyValidator createConsentBodyValidator;
    private final ErrorBuildingService errorBuildingService;

    @Override
    public void validate(HttpServletRequest request, MessageError messageError) {

        // Next step - body validation, should be performed after this.
        Consents body = mapBodyToConsents(request, messageError);

        // TODO: think how to validate body (maybe interface and different implementations with Object in signature)
        createConsentBodyValidator.validateConsentBody(body, messageError);
    }

    private Consents mapBodyToConsents(HttpServletRequest request, MessageError messageError) {

        Consents body = new Consents();

        MultiReadHttpServletRequest multiReadRequest = new MultiReadHttpServletRequest(request);
        try {
            body = objectMapper.readValue(multiReadRequest.getInputStream(), Consents.class);
        } catch (IOException e) {
            errorBuildingService.enrichMessageError(messageError, "Cannot deserialize the request body");
        }

        return body;
    }

}
