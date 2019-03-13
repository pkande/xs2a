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
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.domain.pis.PaymentInitiationParameters;
import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.service.validator.GetCommonPaymentByIdResponseValidator;
import de.adorsys.psd2.xs2a.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.service.validator.tpp.PisTppInfoValidator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static de.adorsys.psd2.xs2a.domain.MessageErrorCode.*;
import static de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType.PIS_400;
import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PaymentServiceValidatorTest {
    private static final PaymentType PAYMENT_TYPE = PaymentType.SINGLE;
    private static final PaymentType INVALID_PAYMENT_TYPE = PaymentType.BULK;
    private static final String PAYMENT_PRODUCT = "payment product";
    private static final String INVALID_PAYMENT_PRODUCT = "invalid payment product";
    private static final TppInfo TPP_INFO = buildTppInfo("authorisation number");
    private static final TppInfo INVALID_TPP_INFO = buildTppInfo("invalid authorisation number");
    private static final PsuIdData PSU_DATA =
        new PsuIdData("psu id", null, null, null);
    private static final PsuIdData EMPTY_PSU_DATA =
        new PsuIdData(null, null, null, null);

    private static final MessageError NO_PSU_ERROR =
        new MessageError(PIS_400, TppMessageInformation.of(FORMAT_ERROR, "Please provide the PSU identification data"));
    private static final MessageError TPP_VALIDATION_ERROR =
        new MessageError(ErrorType.PIS_401, TppMessageInformation.of(UNAUTHORIZED));
    private static final MessageError GET_COMMON_PAYMENT_VALIDATION_ERROR =
        new MessageError(ErrorType.PIS_405, TppMessageInformation.of(SERVICE_INVALID_405));

    @Mock
    private AspspProfileServiceWrapper aspspProfileServiceWrapper;
    @Mock
    private PisTppInfoValidator pisTppInfoValidator;
    @Mock
    private GetCommonPaymentByIdResponseValidator getCommonPaymentByIdResponseValidator;

    @InjectMocks
    private PaymentServiceValidator paymentServiceValidator;

    @Before
    public void setUp() {
        when(pisTppInfoValidator.validateTpp(buildPisCommonPaymentResponse(TPP_INFO)))
            .thenReturn(ValidationResult.valid());
        when(pisTppInfoValidator.validateTpp(buildPisCommonPaymentResponse(INVALID_TPP_INFO)))
            .thenReturn(ValidationResult.invalid(TPP_VALIDATION_ERROR));

        when(getCommonPaymentByIdResponseValidator.validateRequest(buildPisCommonPaymentResponse(TPP_INFO), PAYMENT_TYPE, PAYMENT_PRODUCT))
            .thenReturn(ValidationResult.valid());
        when(getCommonPaymentByIdResponseValidator.validateRequest(buildPisCommonPaymentResponse(INVALID_TPP_INFO), PAYMENT_TYPE, PAYMENT_PRODUCT))
            .thenReturn(ValidationResult.valid());

        when(getCommonPaymentByIdResponseValidator.validateRequest(buildPisCommonPaymentResponse(TPP_INFO), INVALID_PAYMENT_TYPE, INVALID_PAYMENT_PRODUCT))
            .thenReturn(ValidationResult.invalid(GET_COMMON_PAYMENT_VALIDATION_ERROR));
        when(getCommonPaymentByIdResponseValidator.validateRequest(buildPisCommonPaymentResponse(INVALID_TPP_INFO), INVALID_PAYMENT_TYPE, INVALID_PAYMENT_PRODUCT))
            .thenReturn(ValidationResult.invalid(GET_COMMON_PAYMENT_VALIDATION_ERROR));
    }

    @Test
    public void validateCreatePayment_withValidCommonPayment_shouldReturnValid() {
        // Given
        when(aspspProfileServiceWrapper.isPsuInInitialRequestMandated())
            .thenReturn(false);

        PaymentInitiationParameters paymentInitiationParameters = buildPaymentInitiationParameters(PSU_DATA);

        // When
        ValidationResult validationResult = paymentServiceValidator.validateCreatePayment(paymentInitiationParameters);

        // Then
        assertNotNull(validationResult);
        assertTrue(validationResult.isValid());
        assertNull(validationResult.getMessageError());
    }

    @Test
    public void validateCreatePayment_withEmptyPsuDataInParameters_shouldReturnValid() {
        // Given
        when(aspspProfileServiceWrapper.isPsuInInitialRequestMandated())
            .thenReturn(false);

        PaymentInitiationParameters paymentInitiationParameters = buildPaymentInitiationParameters(EMPTY_PSU_DATA);

        // When
        ValidationResult validationResult = paymentServiceValidator.validateCreatePayment(paymentInitiationParameters);

        // Then
        assertNotNull(validationResult);
        assertTrue(validationResult.isValid());
        assertNull(validationResult.getMessageError());
    }

    @Test
    public void validateCreatePayment_withEmptyRequiredPsuDataInParameters_shouldReturnError() {
        // Given
        when(aspspProfileServiceWrapper.isPsuInInitialRequestMandated())
            .thenReturn(true);

        PaymentInitiationParameters paymentInitiationParameters = buildPaymentInitiationParameters(EMPTY_PSU_DATA);

        // When
        ValidationResult validationResult = paymentServiceValidator.validateCreatePayment(paymentInitiationParameters);

        // Then
        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(NO_PSU_ERROR, validationResult.getMessageError());
    }


    @Test
    public void validateGetPaymentById_withValidCommonPayment_shouldReturnValid() {
        // Given
        PisCommonPaymentResponse commonPaymentResponse = buildPisCommonPaymentResponse(TPP_INFO);

        // When
        ValidationResult validationResult = paymentServiceValidator.validateGetPaymentById(commonPaymentResponse, PAYMENT_TYPE, PAYMENT_PRODUCT);

        // Then
        verify(pisTppInfoValidator).validateTpp(commonPaymentResponse);
        verify(getCommonPaymentByIdResponseValidator).validateRequest(commonPaymentResponse, PAYMENT_TYPE, PAYMENT_PRODUCT);

        assertNotNull(validationResult);
        assertTrue(validationResult.isValid());
        assertNull(validationResult.getMessageError());
    }

    @Test
    public void validateGetPaymentById_withInvalidTppInPayment_shouldReturnTppValidationError() {
        // Given
        PisCommonPaymentResponse commonPaymentResponse = buildPisCommonPaymentResponse(INVALID_TPP_INFO);

        // When
        ValidationResult validationResult = paymentServiceValidator.validateGetPaymentById(commonPaymentResponse, PAYMENT_TYPE, PAYMENT_PRODUCT);

        // Then
        verify(pisTppInfoValidator).validateTpp(commonPaymentResponse);

        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(TPP_VALIDATION_ERROR, validationResult.getMessageError());
    }

    @Test
    public void validateGetPaymentById_withInvalidCommonPaymentResponse_shouldReturnGetCommonPaymentValidationError() {
        // Given
        PisCommonPaymentResponse commonPaymentResponse = buildPisCommonPaymentResponse(TPP_INFO);

        // When
        ValidationResult validationResult = paymentServiceValidator.validateGetPaymentById(commonPaymentResponse, INVALID_PAYMENT_TYPE, INVALID_PAYMENT_PRODUCT);

        // Then
        verify(pisTppInfoValidator).validateTpp(commonPaymentResponse);
        verify(getCommonPaymentByIdResponseValidator).validateRequest(commonPaymentResponse, INVALID_PAYMENT_TYPE, INVALID_PAYMENT_PRODUCT);

        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(GET_COMMON_PAYMENT_VALIDATION_ERROR, validationResult.getMessageError());
    }

    @Test
    public void validateGetPaymentById_withInvalidTppAndPaymentResponse_shouldReturnTppValidationError() {
        // Given
        PisCommonPaymentResponse commonPaymentResponse = buildPisCommonPaymentResponse(INVALID_TPP_INFO);

        // When
        ValidationResult validationResult = paymentServiceValidator.validateGetPaymentById(commonPaymentResponse, INVALID_PAYMENT_TYPE, INVALID_PAYMENT_PRODUCT);

        // Then
        verify(pisTppInfoValidator).validateTpp(commonPaymentResponse);

        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(TPP_VALIDATION_ERROR, validationResult.getMessageError());
    }

    @Test
    public void validateGetPaymentStatusById_withValidCommonPayment_shouldReturnValid() {
        // Given
        PisCommonPaymentResponse commonPaymentResponse = buildPisCommonPaymentResponse(TPP_INFO);

        // When
        ValidationResult validationResult = paymentServiceValidator.validateGetPaymentStatusById(commonPaymentResponse, PAYMENT_TYPE, PAYMENT_PRODUCT);

        // Then
        verify(pisTppInfoValidator).validateTpp(commonPaymentResponse);
        verify(getCommonPaymentByIdResponseValidator).validateRequest(commonPaymentResponse, PAYMENT_TYPE, PAYMENT_PRODUCT);

        assertNotNull(validationResult);
        assertTrue(validationResult.isValid());
        assertNull(validationResult.getMessageError());
    }

    @Test
    public void validateGetPaymentStatusById_withInvalidTppInPayment_shouldReturnTppValidationError() {
        // Given
        PisCommonPaymentResponse commonPaymentResponse = buildPisCommonPaymentResponse(INVALID_TPP_INFO);

        // When
        ValidationResult validationResult = paymentServiceValidator.validateGetPaymentStatusById(commonPaymentResponse, PAYMENT_TYPE, PAYMENT_PRODUCT);

        // Then
        verify(pisTppInfoValidator).validateTpp(commonPaymentResponse);

        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(TPP_VALIDATION_ERROR, validationResult.getMessageError());
    }

    @Test
    public void validateGetPaymentStatusById_withInvalidCommonPaymentResponse_shouldReturnGetCommonPaymentValidationError() {
        // Given
        PisCommonPaymentResponse commonPaymentResponse = buildPisCommonPaymentResponse(TPP_INFO);

        // When
        ValidationResult validationResult = paymentServiceValidator.validateGetPaymentStatusById(commonPaymentResponse, INVALID_PAYMENT_TYPE, INVALID_PAYMENT_PRODUCT);

        // Then
        verify(pisTppInfoValidator).validateTpp(commonPaymentResponse);
        verify(getCommonPaymentByIdResponseValidator).validateRequest(commonPaymentResponse, INVALID_PAYMENT_TYPE, INVALID_PAYMENT_PRODUCT);

        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(GET_COMMON_PAYMENT_VALIDATION_ERROR, validationResult.getMessageError());
    }

    @Test
    public void validateGetPaymentStatusById_withInvalidTppAndPaymentResponse_shouldReturnTppValidationError() {
        // Given
        PisCommonPaymentResponse commonPaymentResponse = buildPisCommonPaymentResponse(INVALID_TPP_INFO);

        // When
        ValidationResult validationResult = paymentServiceValidator.validateGetPaymentStatusById(commonPaymentResponse, INVALID_PAYMENT_TYPE, INVALID_PAYMENT_PRODUCT);

        // Then
        verify(pisTppInfoValidator).validateTpp(commonPaymentResponse);

        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(TPP_VALIDATION_ERROR, validationResult.getMessageError());
    }

    @Test
    public void validateCancelPayment_withValidCommonPayment_shouldReturnValid() {
        // Given
        PisCommonPaymentResponse commonPaymentResponse = buildPisCommonPaymentResponse(TPP_INFO);

        // When
        ValidationResult validationResult = paymentServiceValidator.validateCancelPayment(commonPaymentResponse, PAYMENT_TYPE, PAYMENT_PRODUCT);

        // Then
        verify(pisTppInfoValidator).validateTpp(commonPaymentResponse);
        verify(getCommonPaymentByIdResponseValidator).validateRequest(commonPaymentResponse, PAYMENT_TYPE, PAYMENT_PRODUCT);

        assertNotNull(validationResult);
        assertTrue(validationResult.isValid());
        assertNull(validationResult.getMessageError());
    }

    @Test
    public void validateCancelPayment_withInvalidTppInPayment_shouldReturnTppValidationError() {
        // Given
        PisCommonPaymentResponse commonPaymentResponse = buildPisCommonPaymentResponse(INVALID_TPP_INFO);

        // When
        ValidationResult validationResult = paymentServiceValidator.validateCancelPayment(commonPaymentResponse, PAYMENT_TYPE, PAYMENT_PRODUCT);

        // Then
        verify(pisTppInfoValidator).validateTpp(commonPaymentResponse);

        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(TPP_VALIDATION_ERROR, validationResult.getMessageError());
    }

    @Test
    public void validateCancelPayment_withInvalidCommonPaymentResponse_shouldReturnGetCommonPaymentValidationError() {
        // Given
        PisCommonPaymentResponse commonPaymentResponse = buildPisCommonPaymentResponse(TPP_INFO);

        // When
        ValidationResult validationResult = paymentServiceValidator.validateCancelPayment(commonPaymentResponse, INVALID_PAYMENT_TYPE, INVALID_PAYMENT_PRODUCT);

        // Then
        verify(pisTppInfoValidator).validateTpp(commonPaymentResponse);
        verify(getCommonPaymentByIdResponseValidator).validateRequest(commonPaymentResponse, INVALID_PAYMENT_TYPE, INVALID_PAYMENT_PRODUCT);

        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(GET_COMMON_PAYMENT_VALIDATION_ERROR, validationResult.getMessageError());
    }

    @Test
    public void validateCancelPayment_withInvalidTppAndPaymentResponse_shouldReturnTppValidationError() {
        // Given
        PisCommonPaymentResponse commonPaymentResponse = buildPisCommonPaymentResponse(INVALID_TPP_INFO);

        // When
        ValidationResult validationResult = paymentServiceValidator.validateCancelPayment(commonPaymentResponse, INVALID_PAYMENT_TYPE, INVALID_PAYMENT_PRODUCT);

        // Then
        verify(pisTppInfoValidator).validateTpp(commonPaymentResponse);

        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(TPP_VALIDATION_ERROR, validationResult.getMessageError());
    }

    private PaymentInitiationParameters buildPaymentInitiationParameters(PsuIdData psuIdData) {
        PaymentInitiationParameters requestParameters = new PaymentInitiationParameters();
        requestParameters.setPsuData(psuIdData);
        return requestParameters;
    }

    private static TppInfo buildTppInfo(String authorisationNumber) {
        TppInfo tppInfo = new TppInfo();
        tppInfo.setAuthorisationNumber(authorisationNumber);
        return tppInfo;
    }

    private PisCommonPaymentResponse buildPisCommonPaymentResponse(TppInfo tppInfo) {
        PisCommonPaymentResponse pisCommonPaymentResponse = new PisCommonPaymentResponse();
        pisCommonPaymentResponse.setTppInfo(tppInfo);
        return pisCommonPaymentResponse;
    }
}
