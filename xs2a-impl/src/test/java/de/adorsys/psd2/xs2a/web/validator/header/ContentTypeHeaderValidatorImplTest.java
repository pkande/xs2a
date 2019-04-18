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

import de.adorsys.psd2.xs2a.domain.ContentType;
import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
import de.adorsys.psd2.xs2a.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.web.validator.ErrorBuildingService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

import static de.adorsys.psd2.xs2a.web.validator.header.AbstractHeaderValidatorImpl.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ContentTypeHeaderValidatorImplTest {

    @Mock
    private ErrorBuildingService errorBuildingService;
    private ContentTypeHeaderValidatorImpl validator;

    @Before
    public void setUp() {
        validator = new ContentTypeHeaderValidatorImpl(errorBuildingService);

        when(errorBuildingService.buildErrorType()).thenReturn(ErrorType.AIS_400);
    }

    @Test
    public void checkIfHeaderIsPresented_success() {
        Map<String, String> headers = new HashMap<>();
        headers.put(validator.getHeaderName(), ContentType.JSON.getType());
        ValidationResult validationResult = validator.checkIfHeaderIsPresented(headers);
        assertTrue(validationResult.isValid());
    }

    @Test
    public void checkIfHeaderIsPresented_errors() {
        Map<String, String> headers = new HashMap<>();
        ValidationResult validationResult = validator.checkIfHeaderIsPresented(headers);
        checkError(validationResult, ErrorType.AIS_400, String.format(ERROR_TEXT_ABSENT_HEADER, validator.getHeaderName()));

        headers.put(validator.getHeaderName(), null);
        validationResult = validator.checkIfHeaderIsPresented(headers);
        checkError(validationResult, ErrorType.AIS_400, String.format(ERROR_TEXT_NULL_HEADER, validator.getHeaderName()));

        headers.put(validator.getHeaderName(), "");
        validationResult = validator.checkIfHeaderIsPresented(headers);
        checkError(validationResult, ErrorType.AIS_400, String.format(ERROR_TEXT_BLANK_HEADER, validator.getHeaderName()));
    }

    private void checkError(ValidationResult validationResult, ErrorType errorType, String message) {
        assertFalse(validationResult.isValid());
        MessageError messageError = validationResult.getMessageError();
        assertEquals(errorType, messageError.getErrorType());
        assertEquals(1, messageError.getTppMessages().size());
        messageError.getTppMessages().forEach(i-> assertEquals(message, i.getText()));

    }
}
