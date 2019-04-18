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

import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.web.validator.ErrorBuildingService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

import static de.adorsys.psd2.xs2a.web.validator.constants.Xs2aHeaderConstant.*;

/**
 * Service to be used to validate the length of headers (exceeding the max length).
 */
@Service
@RequiredArgsConstructor
public class HeaderLengthValidationService {

    // Array of headers that should be checked for the text length:
    private static final String[] HEADERS_TO_VALIDATE = {PSU_ID, PSU_ID_TYPE, PSU_CORPORATE_ID, PSU_CORPORATE_ID_TYPE, AUTHORISATION, TPP_REDIRECT_URI, TPP_NOK_REDIRECT_URI};
    private static final int MAX_HEADER_LENGTH = 140;
    private final ErrorBuildingService errorBuildingService;

    public void validateHeaders(MessageError messageError, HttpServletRequest request) {

        Map<String, String> headers = Collections.list(request.getHeaderNames())
                                          .stream()
                                          .collect(Collectors.toMap(h -> h, request::getHeader));

        List<String> wrongLengthHeaders = new ArrayList<>();

        headers.forEach((k, v) -> {
            if (Arrays.stream(HEADERS_TO_VALIDATE).anyMatch(h -> h.equalsIgnoreCase(k))) {
                if (v.length() > MAX_HEADER_LENGTH) {
                    wrongLengthHeaders.add(k);
                }
            }
        });

        if (CollectionUtils.isNotEmpty(wrongLengthHeaders)) {
            getResultWithError(messageError, wrongLengthHeaders);
        }
    }

    private void getResultWithError(MessageError messageError, List<String> wrongLengthHeaders) {
        wrongLengthHeaders.forEach(h -> {
            String resultingMessage = String.format("Header '%s' should not be more than %s symbols", h, MAX_HEADER_LENGTH);
            errorBuildingService.enrichMessageError(messageError, resultingMessage);
        });
    }
}
