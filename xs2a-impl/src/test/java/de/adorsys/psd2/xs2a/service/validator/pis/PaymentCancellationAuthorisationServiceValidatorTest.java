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

package de.adorsys.psd2.xs2a.service.validator.pis;

import de.adorsys.psd2.consent.api.pis.proto.PisCommonPaymentResponse;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.domain.pis.PaymentAuthorisationType;
import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
import de.adorsys.psd2.xs2a.service.validator.PisEndpointAccessCheckerService;
import de.adorsys.psd2.xs2a.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.service.validator.tpp.PisTppInfoValidator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;

import static de.adorsys.psd2.xs2a.domain.MessageErrorCode.*;
import static de.adorsys.psd2.xs2a.domain.TppMessageInformation.of;
import static de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType.PIS_401;
import static de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType.PIS_403;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PaymentCancellationAuthorisationServiceValidatorTest {
    private static final TppInfo TPP_INFO = buildTppInfo("authorisation number");
    private static final TppInfo INVALID_TPP_INFO = buildTppInfo("invalid authorisation number");
    private static final String AUTHORISATION_ID = "authorisation id";
    private static final String INVALID_AUTHORISATION_ID = "invalid authorisation id";

    private static final PsuIdData PSU_DATA = new PsuIdData("psu id", null, null, null);
    private static final PsuIdData EMPTY_PSU_DATA = new PsuIdData(null, null, null, null);
    private static final PsuIdData INVALID_PSU_DATA = new PsuIdData("invalid psu id", null, null, null);

    private static final MessageError TPP_VALIDATION_ERROR =
        new MessageError(ErrorType.PIS_401, TppMessageInformation.of(UNAUTHORIZED, "Invalid TPP"));
    private static final MessageError INVALID_PSU_ERROR = new MessageError(PIS_401, of(PSU_CREDENTIALS_INVALID));
    private static final MessageError BLOCKED_ENDPOINT_ERROR = new MessageError(PIS_403, of(SERVICE_BLOCKED));

    @Mock
    private PisTppInfoValidator pisTppInfoValidator;
    @Mock
    private PisEndpointAccessCheckerService pisEndpointAccessCheckerService;

    @InjectMocks
    private PaymentCancellationAuthorisationServiceValidator paymentCancellationAuthorisationServiceValidator;

    @Before
    public void setUp() {
        when(pisTppInfoValidator.validateTpp(buildPisCommonPaymentResponse(TPP_INFO)))
            .thenReturn(ValidationResult.valid());
        when(pisTppInfoValidator.validateTpp(buildPisCommonPaymentResponse(TPP_INFO)))
            .thenReturn(ValidationResult.valid());
        when(pisTppInfoValidator.validateTpp(buildPisCommonPaymentResponse(INVALID_TPP_INFO)))
            .thenReturn(ValidationResult.invalid(TPP_VALIDATION_ERROR));
        when(pisTppInfoValidator.validateTpp(buildPisCommonPaymentResponse(INVALID_TPP_INFO)))
            .thenReturn(ValidationResult.invalid(TPP_VALIDATION_ERROR));

        when(pisEndpointAccessCheckerService.isEndpointAccessible(AUTHORISATION_ID, PaymentAuthorisationType.CANCELLATION))
            .thenReturn(true);
        when(pisEndpointAccessCheckerService.isEndpointAccessible(INVALID_AUTHORISATION_ID, PaymentAuthorisationType.CANCELLATION))
            .thenReturn(false);
    }

    @Test
    public void validateCreatePisCancellationAuthorisation_withValidCommonPayment_shouldReturnValid() {
        // Given
        PisCommonPaymentResponse commonPaymentResponse = buildPisCommonPaymentResponse(TPP_INFO);

        // When
        ValidationResult validationResult =
            paymentCancellationAuthorisationServiceValidator.validateCreatePisCancellationAuthorisation(commonPaymentResponse, PSU_DATA);

        // Then
        assertNotNull(validationResult);
        assertTrue(validationResult.isValid());
        assertNull(validationResult.getMessageError());
    }

    @Test
    public void validateCreatePisCancellationAuthorisation_withValidCommonPaymentAndEmptyPsuData_shouldReturnValid() {
        // Given
        PisCommonPaymentResponse commonPaymentResponse = buildPisCommonPaymentResponse(TPP_INFO);

        // When
        ValidationResult validationResult =
            paymentCancellationAuthorisationServiceValidator.validateCreatePisCancellationAuthorisation(commonPaymentResponse, EMPTY_PSU_DATA);

        // Then
        assertNotNull(validationResult);
        assertTrue(validationResult.isValid());
        assertNull(validationResult.getMessageError());
    }

    @Test
    public void validateCreatePisCancellationAuthorisation_withInvalidTppInPayment_shouldReturnError() {
        // Given
        PisCommonPaymentResponse commonPaymentResponse = buildPisCommonPaymentResponse(INVALID_TPP_INFO);

        // When
        ValidationResult validationResult =
            paymentCancellationAuthorisationServiceValidator.validateCreatePisCancellationAuthorisation(commonPaymentResponse, PSU_DATA);

        // Then
        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(TPP_VALIDATION_ERROR, validationResult.getMessageError());
    }


    @Test
    public void validateCreatePisCancellationAuthorisation_withInvalidPsuData_shouldReturnError() {
        // Given
        PisCommonPaymentResponse commonPaymentResponse = buildPisCommonPaymentResponse(TPP_INFO);

        // When
        ValidationResult validationResult =
            paymentCancellationAuthorisationServiceValidator.validateCreatePisCancellationAuthorisation(commonPaymentResponse, INVALID_PSU_DATA);

        // Then
        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(INVALID_PSU_ERROR, validationResult.getMessageError());
    }

    @Test
    public void validateCreatePisCancellationAuthorisation_withInvalidPsuDataAndInvalidTppInPayment_shouldReturnTppValidationError() {
        // Given
        PisCommonPaymentResponse commonPaymentResponse = buildPisCommonPaymentResponse(INVALID_TPP_INFO);

        // When
        ValidationResult validationResult =
            paymentCancellationAuthorisationServiceValidator.validateCreatePisCancellationAuthorisation(commonPaymentResponse, INVALID_PSU_DATA);

        // Then
        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(TPP_VALIDATION_ERROR, validationResult.getMessageError());
    }

    @Test
    public void validateUpdatePisCancellationPsuData_withValidCommonPayment_shouldReturnValid() {
        // Given
        PisCommonPaymentResponse commonPaymentResponse = buildPisCommonPaymentResponse(TPP_INFO);

        // When
        ValidationResult validationResult =
            paymentCancellationAuthorisationServiceValidator.validateUpdatePisCancellationPsuData(commonPaymentResponse, AUTHORISATION_ID);

        // Then
        assertNotNull(validationResult);
        assertTrue(validationResult.isValid());
        assertNull(validationResult.getMessageError());
    }

    @Test
    public void validateUpdatePisCancellationPsuData_withInvalidTppInPayment_shouldReturnError() {
        // Given
        PisCommonPaymentResponse commonPaymentResponse = buildPisCommonPaymentResponse(INVALID_TPP_INFO);

        // When
        ValidationResult validationResult =
            paymentCancellationAuthorisationServiceValidator.validateUpdatePisCancellationPsuData(commonPaymentResponse, AUTHORISATION_ID);

        // Then
        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(TPP_VALIDATION_ERROR, validationResult.getMessageError());
    }


    @Test
    public void validateUpdatePisCancellationPsuData_withInvalidAuthorisation_shouldReturnError() {
        // Given
        PisCommonPaymentResponse commonPaymentResponse = buildPisCommonPaymentResponse(TPP_INFO);

        // When
        ValidationResult validationResult =
            paymentCancellationAuthorisationServiceValidator.validateUpdatePisCancellationPsuData(commonPaymentResponse, INVALID_AUTHORISATION_ID);

        // Then
        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(BLOCKED_ENDPOINT_ERROR, validationResult.getMessageError());
    }

    @Test
    public void validateUpdatePisCancellationPsuData_withInvalidAuthorisationAndInvalidTppInPayment_shouldReturnTppValidationError() {
        // Given
        PisCommonPaymentResponse commonPaymentResponse = buildPisCommonPaymentResponse(INVALID_TPP_INFO);

        // When
        ValidationResult validationResult =
            paymentCancellationAuthorisationServiceValidator.validateUpdatePisCancellationPsuData(commonPaymentResponse, INVALID_AUTHORISATION_ID);

        // Then
        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(TPP_VALIDATION_ERROR, validationResult.getMessageError());
    }

    @Test
    public void validateGetPaymentInitiationCancellationAuthorisationInformation_withValidCommonPayment_shouldReturnValid() {
        // Given
        PisCommonPaymentResponse commonPaymentResponse = buildPisCommonPaymentResponse(TPP_INFO);

        // When
        ValidationResult validationResult = paymentCancellationAuthorisationServiceValidator.validateGetPaymentInitiationCancellationAuthorisationInformation(commonPaymentResponse);

        // Then
        assertNotNull(validationResult);
        assertTrue(validationResult.isValid());
        assertNull(validationResult.getMessageError());
    }

    @Test
    public void validateGetPaymentInitiationCancellationAuthorisationInformation_withInvalidTppInPayment_shouldReturnError() {
        // Given
        PisCommonPaymentResponse commonPaymentResponse = buildPisCommonPaymentResponse(INVALID_TPP_INFO);

        // When
        ValidationResult validationResult = paymentCancellationAuthorisationServiceValidator.validateGetPaymentInitiationCancellationAuthorisationInformation(commonPaymentResponse);

        // Then
        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(TPP_VALIDATION_ERROR, validationResult.getMessageError());
    }

    @Test
    public void validateGetPaymentCancellationAuthorisationScaStatus_withValidCommonPayment_shouldReturnValid() {
        // Given
        PisCommonPaymentResponse commonPaymentResponse = buildPisCommonPaymentResponse(TPP_INFO);

        // When
        ValidationResult validationResult = paymentCancellationAuthorisationServiceValidator.validateGetPaymentCancellationAuthorisationScaStatus(commonPaymentResponse);

        // Then
        assertNotNull(validationResult);
        assertTrue(validationResult.isValid());
        assertNull(validationResult.getMessageError());
    }

    @Test
    public void validateGetPaymentCancellationAuthorisationScaStatus_withInvalidTppInPayment_shouldReturnError() {
        // Given
        PisCommonPaymentResponse commonPaymentResponse = buildPisCommonPaymentResponse(INVALID_TPP_INFO);

        // When
        ValidationResult validationResult = paymentCancellationAuthorisationServiceValidator.validateGetPaymentCancellationAuthorisationScaStatus(commonPaymentResponse);

        // Then
        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(TPP_VALIDATION_ERROR, validationResult.getMessageError());
    }

    @Test
    public void validateUpdatePisCancellationPsuData() {
    }

    @Test
    public void validateGetPaymentInitiationCancellationAuthorisationInformation() {
    }

    @Test
    public void validateGetPaymentCancellationAuthorisationScaStatus() {
    }

    private static TppInfo buildTppInfo(String authorisationNumber) {
        TppInfo tppInfo = new TppInfo();
        tppInfo.setAuthorisationNumber(authorisationNumber);
        return tppInfo;
    }

    private PisCommonPaymentResponse buildPisCommonPaymentResponse(TppInfo tppInfo) {
        PisCommonPaymentResponse pisCommonPaymentResponse = new PisCommonPaymentResponse();
        pisCommonPaymentResponse.setTppInfo(tppInfo);
        pisCommonPaymentResponse.setPsuData(Collections.singletonList(PSU_DATA));
        return pisCommonPaymentResponse;
    }
}
