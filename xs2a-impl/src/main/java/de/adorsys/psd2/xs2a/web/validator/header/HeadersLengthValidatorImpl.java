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
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

import static de.adorsys.psd2.xs2a.web.validator.constants.Xs2aHeaderConstant.HEADERS_MAP;


@Component
public class HeadersLengthValidatorImpl extends AbstractHeaderValidatorImpl
    implements ConsentHeaderValidator, PaymentHeaderValidator {

    private static final String HEADER_LENGTH_ERROR_TEXT = "Header '%s' should not be more than %s symbols";

    @Autowired
    public HeadersLengthValidatorImpl(ErrorBuildingService errorBuildingService) {
        super(errorBuildingService);
    }

    @Override
    protected String getHeaderName() {
        return null;
    }

    @Override
    public void validate(Map<String, String> inputHeaders, MessageError messageError) {
        Map<Integer, List<String>> wrongLengthHeaders1 = new HashMap<>();

        HEADERS_MAP.forEach((length, array) -> validateByLength(inputHeaders, array, wrongLengthHeaders1, length));

        if (MapUtils.isNotEmpty(wrongLengthHeaders1)) {
            getResultWithError(messageError, wrongLengthHeaders1);
        }
    }

    private void validateByLength(Map<String, String> headers, String[] headersToBeValidated, Map<Integer, List<String>> wrongLengthHeaders1, int length) {
        headers.forEach((k, v) -> {
            if (Arrays.stream(headersToBeValidated).anyMatch(h -> h.equalsIgnoreCase(k)) && v.length() > length) {
                if (!wrongLengthHeaders1.containsKey(length)) {
                    wrongLengthHeaders1.put(length, new ArrayList<String>() {{
                        add(k);
                    }});
                } else {
                    wrongLengthHeaders1.get(length).add(k);
                }
            }
        });
    }

    private void getResultWithError(MessageError messageError, Map<Integer, List<String>> wrongLengthHeaders1) {
        wrongLengthHeaders1.forEach((length, listOfHeaders) -> listOfHeaders.forEach(h -> {
            String resultingMessage = String.format(HEADER_LENGTH_ERROR_TEXT, h, length);
            errorBuildingService.enrichMessageError(messageError, resultingMessage);
        }));
    }
}
