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

package de.adorsys.psd2.xs2a.web.validator.common.service;

import de.adorsys.psd2.xs2a.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.service.discovery.ServiceTypeDiscoveryService;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ServiceTypeToErrorTypeMapper;
import de.adorsys.psd2.xs2a.service.validator.ValidationResult;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;

import static de.adorsys.psd2.xs2a.domain.MessageErrorCode.FORMAT_ERROR;
import static de.adorsys.psd2.xs2a.web.validator.constants.Xs2aHeaderConstant.CONTENT_TYPE;

/**
 * Service to be used to validate 'Content-type' header in all REST calls.
 */
@Service
@RequiredArgsConstructor
public class ContentTypeValidationService implements OneHeaderValidator {

    private static final String ERROR_TEXT_ABSENT_HEADER = "Header 'Content-type' is missing in request";
    private static final String ERROR_TEXT_NULL_HEADER = "Header 'Content-type' may not be null";
    private static final String ERROR_TEXT_BLANK_HEADER = "Header 'Content-type' may not be blank";

    private final ServiceTypeDiscoveryService serviceTypeDiscoveryService;
    private final ServiceTypeToErrorTypeMapper errorTypeMapper;

    @Override
    public ValidationResult validateHeader(Map<String, String> headers) {

        if (!headers.containsKey(CONTENT_TYPE)) {
            return ValidationResult.invalid(buildErrorType(), TppMessageInformation.of(FORMAT_ERROR, ERROR_TEXT_ABSENT_HEADER));
        }

        String contentType = headers.get(CONTENT_TYPE);

        if (Objects.isNull(contentType)) {
            return ValidationResult.invalid(buildErrorType(), TppMessageInformation.of(FORMAT_ERROR, ERROR_TEXT_NULL_HEADER));
        }

        if (StringUtils.isBlank(contentType)) {
            return ValidationResult.invalid(buildErrorType(), TppMessageInformation.of(FORMAT_ERROR, ERROR_TEXT_BLANK_HEADER));
        }

        return ValidationResult.valid();
    }

    private ErrorType buildErrorType() {
        return errorTypeMapper.mapToErrorType(serviceTypeDiscoveryService.getServiceType(), FORMAT_ERROR.getCode());
    }
}
