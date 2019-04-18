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

package de.adorsys.psd2.xs2a.web.validator.header;

import de.adorsys.psd2.xs2a.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.service.discovery.ServiceTypeDiscoveryService;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ServiceTypeToErrorTypeMapper;
import de.adorsys.psd2.xs2a.service.validator.ValidationResult;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Objects;

import static de.adorsys.psd2.xs2a.domain.MessageErrorCode.FORMAT_ERROR;

public abstract class AbstractHeaderValidatorImpl implements HeaderValidator {

    private static final String ERROR_TEXT_ABSENT_HEADER = "Header '%s' is missing in request";
    private static final String ERROR_TEXT_NULL_HEADER = "Header '%s' may not be null";
    private static final String ERROR_TEXT_BLANK_HEADER = "Header '%s' may not be blank";

    protected ServiceTypeDiscoveryService serviceTypeDiscoveryService;
    protected ServiceTypeToErrorTypeMapper errorTypeMapper;

    protected abstract String getHeaderName();

    @Override
    public void validate(Map<String, String> headers, HttpServletRequest request, MessageError messageError) {
        ValidationResult validationResult = validate(headers);

        if (validationResult.isNotValid()) {
            enrichMessageError(messageError, validationResult.getMessageError());
        }
    }

    protected ValidationResult validate(Map<String, String> headers) {
        checkIfHeaderIsPresented(headers);
        checkHeaderContent(headers);

        return ValidationResult.valid();
    }

    protected ValidationResult checkHeaderContent(Map<String, String> headers) {
        return ValidationResult.valid();
    }

    ValidationResult checkIfHeaderIsPresented(Map<String, String> headers) {
        if (!headers.containsKey(getHeaderName())) {
            return ValidationResult.invalid(
                buildErrorType(), TppMessageInformation.of(FORMAT_ERROR,
                                                           String.format(ERROR_TEXT_ABSENT_HEADER, getHeaderName())));
        }

        String contentType = headers.get(getHeaderName());
        if (Objects.isNull(contentType)) {
            return ValidationResult.invalid(
                buildErrorType(), TppMessageInformation.of(FORMAT_ERROR,
                                                           String.format(ERROR_TEXT_NULL_HEADER, getHeaderName())));
        }

        if (StringUtils.isBlank(contentType)) {
            return ValidationResult.invalid(
                buildErrorType(), TppMessageInformation.of(FORMAT_ERROR,
                                                           String.format(ERROR_TEXT_BLANK_HEADER, getHeaderName())));
        }
        return ValidationResult.valid();
    }

    ErrorType buildErrorType() {
        return errorTypeMapper.mapToErrorType(serviceTypeDiscoveryService.getServiceType(), FORMAT_ERROR.getCode());
    }

    void enrichMessageError(MessageError messageError, MessageError validationMessageError) {
        enrichMessageError(messageError, validationMessageError.getTppMessage());
    }

    void enrichMessageError(MessageError messageError, TppMessageInformation tppMessageInformation) {
        messageError.addTppMessage(tppMessageInformation);
    }

    void enrichMessageError(MessageError messageError, String errorMessage) {
        enrichMessageError(messageError, new MessageError(buildErrorType(), TppMessageInformation.of(FORMAT_ERROR, errorMessage)));
    }
}
