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

package de.adorsys.psd2.xs2a.service.validator;

import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.core.profile.AccountReferenceType;
import de.adorsys.psd2.xs2a.domain.account.SupportedAccountReferenceField;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.discovery.ServiceTypeDiscoveryService;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ServiceTypeToErrorTypeMapper;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

import static de.adorsys.psd2.xs2a.domain.MessageErrorCode.FORMAT_ERROR;

@Slf4j
@Component
@RequiredArgsConstructor
public class SupportedAccountReferenceValidator implements BusinessValidator<Collection<AccountReference>> {
    private final AspspProfileServiceWrapper aspspProfileService;
    private final RequestProviderService requestProviderService;
    private final ServiceTypeDiscoveryService serviceTypeDiscoveryService;
    private final ServiceTypeToErrorTypeMapper errorTypeMapper;

    @Override
    public @NotNull ValidationResult validate(@NotNull Collection<AccountReference> accountReferences) {
        List<SupportedAccountReferenceField> supportedAccountReferenceFields = aspspProfileService.getSupportedAccountReferenceFields();
        boolean allReferencesTypeSupported = accountReferences.stream()
                                                 .allMatch(ar -> isAccountReferenceTypeSupported(ar, supportedAccountReferenceFields));

        if (!allReferencesTypeSupported) {
            log.info("X-Request-ID: [{}]. Supported account reference validation has failed: account reference type is not supported by the ASPSP",
                     requestProviderService.getRequestId());

            ErrorType errorType = errorTypeMapper.mapToErrorType(serviceTypeDiscoveryService.getServiceType(),
                                                                 FORMAT_ERROR.getCode());
            return ValidationResult.invalid(errorType, FORMAT_ERROR);
        }

        return ValidationResult.valid();
    }

    private boolean isAccountReferenceTypeSupported(AccountReference accountReference,
                                                    Collection<SupportedAccountReferenceField> supportedFields) {
        return supportedFields.stream()
                   .map(f -> AccountReferenceType.valueOf(f.name()))
                   .anyMatch(rt -> accountReference.getUsedAccountReferenceSelector().getAccountReferenceType().equals(rt));
    }
}
