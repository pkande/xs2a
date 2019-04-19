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

package de.adorsys.psd2.xs2a.web.validator.methods;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.adorsys.psd2.model.AccountAccess;
import de.adorsys.psd2.model.AccountReference;
import de.adorsys.psd2.model.Consents;
import de.adorsys.psd2.xs2a.component.MultiReadHttpServletRequest;
import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.web.validator.ErrorBuildingService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.IBANValidator;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Currency;
import java.util.List;
import java.util.Objects;

public class AccountAccessValidatorImpl extends AbstractBodyValidatorImpl {

    public AccountAccessValidatorImpl(ErrorBuildingService errorBuildingService, ObjectMapper objectMapper) {
        super(errorBuildingService, objectMapper);
    }

    @Override
    public void validate(HttpServletRequest request, MessageError messageError) {

        Consents consents = mapBodyToConsents(request, messageError);

        // In case of wrong JSON - we don't proceed the inner fields validation.
        if (consents == null) {
            return;
        }

        if (Objects.isNull(consents.getAccess())) {
            errorBuildingService.enrichMessageError(messageError, "Value 'access' should not be null");
        } else {
            validateAccountAccess(consents.getAccess(), messageError);
        }
    }

    private Consents mapBodyToConsents(HttpServletRequest request, MessageError messageError) {

        Consents body = null;

        MultiReadHttpServletRequest multiReadRequest = new MultiReadHttpServletRequest(request);
        try {
            body = objectMapper.readValue(multiReadRequest.getInputStream(), Consents.class);
        } catch (IOException e) {
            errorBuildingService.enrichMessageError(messageError, "Cannot deserialize the request body");
        }

        return body;
    }

    private void validateAccountAccess(AccountAccess accountAccess, MessageError messageError) {
        List<AccountReference> accountAccesses = accountAccess.getAccounts();
        accountAccesses.addAll(accountAccess.getBalances());
        accountAccesses.addAll(accountAccess.getTransactions());

        if (CollectionUtils.isNotEmpty(accountAccesses)) {
            accountAccesses.forEach(ar -> validateAccountReference(ar, messageError));
        }
    }

    private void validateAccountReference(AccountReference accountReference, MessageError messageError) {
        if (StringUtils.isNotBlank(accountReference.getIban()) && !isValidIban(accountReference.getIban())) {
            errorBuildingService.enrichMessageError(messageError, "Invalid IBAN format");
        }
        if (StringUtils.isNotBlank(accountReference.getBban()) && !isValidBban(accountReference.getBban())) {
            errorBuildingService.enrichMessageError(messageError, "Invalid BBAN format");
        }
        if (StringUtils.isNotBlank(accountReference.getPan())) {
            checkFieldForMaxLength(accountReference.getPan(), "PAN", 35, messageError);
        }
        if (StringUtils.isNotBlank(accountReference.getMaskedPan())) {
            checkFieldForMaxLength(accountReference.getMaskedPan(), "Masked PAN", 35, messageError);
        }
        if (StringUtils.isNotBlank(accountReference.getMsisdn())) {
            checkFieldForMaxLength(accountReference.getMsisdn(), "MSISDN", 35, messageError);
        }
        if (StringUtils.isNotBlank(accountReference.getCurrency()) && !isValidCurrency(accountReference.getCurrency())) {
            errorBuildingService.enrichMessageError(messageError, "Invalid currency code format");
        }
    }

    private boolean isValidIban(String iban) {
        IBANValidator validator = IBANValidator.getInstance();
        return validator.isValid(normalizeString(iban));
    }

    private boolean isValidBban(String bban) {
        return normalizeString(bban).length() >= 11
                   && normalizeString(bban).length() <= 28; // Can be extended with aprox 50 country specific masks
    }

    private boolean isValidCurrency(String currency) {
        try {
            Currency.getInstance(currency);
        } catch (IllegalArgumentException e) {
            return false;
        }
        return true;
    }

    private String normalizeString(String string) {
        return string.replaceAll("[^a-zA-Z0-9]", "");
    }
}
