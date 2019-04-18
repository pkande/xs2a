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

package de.adorsys.psd2.xs2a.web.validator;

import de.adorsys.psd2.xs2a.web.validator.header.*;
import de.adorsys.psd2.xs2a.web.validator.methods.BodyValidator;

import java.util.ArrayList;
import java.util.List;

public class PaymentMethodValidatorImpl extends AbstractMethodValidator {

    private ErrorBuildingService errorBuildingService;
    private List<HeaderValidator> headerValidators = new ArrayList<>();

    PaymentMethodValidatorImpl(ErrorBuildingService errorBuildingService) {
        this.errorBuildingService = errorBuildingService;

        populateHeaderValidators();
    }

    @Override
    public List<HeaderValidator> getHeaderValidators() {
        return headerValidators;
    }

    @Override
    protected List<BodyValidator> getBodyValidators() {
        return new ArrayList<>();
    }

    private void populateHeaderValidators() {
        //Common header validators
        headerValidators.add(new ContentTypeHeaderValidatorImpl(errorBuildingService));
        headerValidators.add(new XRequestIdHeaderValidatorImpl(errorBuildingService));
        headerValidators.add(new HeadersLengthValidatorImpl(errorBuildingService));

        //Specific header validators
        headerValidators.add(new PsuIPAddressHeaderValidatorImpl(errorBuildingService));
        headerValidators.add(new TppRedirectPreferredHeaderValidatorImpl(errorBuildingService));
        headerValidators.add(new TppRejectionNoFundsPrefferedHeaderValidationImpl(errorBuildingService));
        headerValidators.add(new TppExplicitAuthorisationPrefferredHeaderValidatorImpl(errorBuildingService));
    }
}
