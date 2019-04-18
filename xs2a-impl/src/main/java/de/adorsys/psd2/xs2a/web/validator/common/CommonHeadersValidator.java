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

package de.adorsys.psd2.xs2a.web.validator.common;

import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.web.validator.ErrorBuildingService;
import de.adorsys.psd2.xs2a.web.validator.common.service.ContentTypeValidationService;
import de.adorsys.psd2.xs2a.web.validator.common.service.XRequestIdValidationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This class is used for validation of common headers, that are used in all controllers' methods.
 */
@Component
@RequiredArgsConstructor
public class CommonHeadersValidator {

    private final XRequestIdValidationService xRequestIdValidationService;
    private final ContentTypeValidationService contentTypeValidationService;
    private final ErrorBuildingService errorBuildingService;

    public void validate(MessageError messageError, HttpServletRequest request) {

        Map<String, String> headers = Collections.list(request.getHeaderNames())
                                          .stream()
                                          .collect(Collectors.toMap(h -> h, request::getHeader));

        ValidationResult xRequestIdValidationResult = xRequestIdValidationService.validateHeader(headers);

        if (xRequestIdValidationResult.isNotValid()) {
            errorBuildingService.enrichMessageError(messageError, xRequestIdValidationResult.getMessageError());
        }

        ValidationResult contentTypeValidationResult = contentTypeValidationService.validateHeader(headers);

        if (contentTypeValidationResult.isNotValid()) {
            errorBuildingService.enrichMessageError(messageError, contentTypeValidationResult.getMessageError());
        }
    }
}
