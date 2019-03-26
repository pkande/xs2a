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

package de.adorsys.psd2.xs2a.service.validator.account;

import de.adorsys.psd2.xs2a.domain.consent.AccountConsent;
import de.adorsys.psd2.xs2a.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.service.validator.account.common.AccountConsentValidator;
import de.adorsys.psd2.xs2a.service.validator.account.common.PermittedAccountReferenceValidator;
import de.adorsys.psd2.xs2a.service.validator.tpp.AisTppInfoValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetTransactionsReportByPeriodValidator {
    private final AisTppInfoValidator aisTppInfoValidator;
    private final AccountConsentValidator accountConsentValidator;
    private final PermittedAccountReferenceValidator permittedAccountReferenceValidator;

    public ValidationResult validate(AccountConsent accountConsent, String accountId, boolean withBalance) {
        ValidationResult permittedAccountReferenceValidationResult =
            permittedAccountReferenceValidator.validate(accountConsent, accountConsent.getAccess().getTransactions(), accountId, withBalance);

        if (permittedAccountReferenceValidationResult.isNotValid()) {
            return permittedAccountReferenceValidationResult;
        }

        ValidationResult tppValidationResult = aisTppInfoValidator.validateTpp(accountConsent.getTppInfo());

        if (tppValidationResult.isNotValid()) {
            return tppValidationResult;
        }

        return accountConsentValidator.validate(accountConsent);
    }
}
