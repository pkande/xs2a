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

import de.adorsys.psd2.xs2a.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
import de.adorsys.psd2.xs2a.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.web.validator.ErrorBuildingService;
import de.adorsys.psd2.xs2a.web.validator.methods.MethodHeadersValidator;
import de.adorsys.psd2.xs2a.web.validator.methods.service.PsuIpAddressValidationService;
import de.adorsys.psd2.xs2a.web.validator.methods.service.TppRedirectUriValidationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static de.adorsys.psd2.xs2a.domain.MessageErrorCode.FORMAT_ERROR;
import static de.adorsys.psd2.xs2a.web.validator.constants.Xs2aHeaderConstant.*;

@RequiredArgsConstructor
@Service("_initiatePayment")
public class InitiatePaymentValidator implements MethodHeadersValidator {

    // Headers that should be validated here:
    // TODO: enrich the list:
    // PSU-ID, PSU-ID-Type, ....
    // https://wiki.adorsys.de/display/PSD2/VR-+002+PIS+Fields+validation

    @Override
    public void validate(HttpServletRequest request, MessageError messageError) {

        validatePsuId(request, messageError);
        validatePsuIdType(request, messageError);
        // TODO:
    }

    private void validatePsuId(HttpServletRequest request, MessageError messageError) {
        // TODO:
    }

    private void validatePsuIdType(HttpServletRequest request, MessageError messageError) {
        // TODO:
    }
}
