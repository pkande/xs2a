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

import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.web.validator.ErrorBuildingService;
import org.apache.commons.collections4.CollectionUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static de.adorsys.psd2.xs2a.web.validator.constants.Xs2aHeaderConstant.*;

public class HeadersLengthValidatorImpl extends AbstractHeaderValidatorImpl {

    // Array of headers that should be checked for the text length:
    private static final String[] HEADERS_TO_VALIDATE = {PSU_ID, PSU_ID_TYPE, PSU_CORPORATE_ID, PSU_CORPORATE_ID_TYPE,
        AUTHORISATION, TPP_REDIRECT_URI, TPP_NOK_REDIRECT_URI, PSU_IP_ADDRESS};
    private static final int MAX_HEADER_LENGTH = 140;

    private static final String HEADER_LENGHT_ERROR_TEXT = "Header '%s' should not be more than %s symbols";

    public HeadersLengthValidatorImpl(ErrorBuildingService errorBuildingService) {
        super(errorBuildingService);
    }

    @Override
    protected String getHeaderName() {
        return null;
    }

    @Override
    public void validate(Map<String, String> headers, MessageError messageError) {
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
            String resultingMessage = String.format(HEADER_LENGHT_ERROR_TEXT, h, MAX_HEADER_LENGTH);
            errorBuildingService.enrichMessageError(messageError, resultingMessage);
        });
    }
}
