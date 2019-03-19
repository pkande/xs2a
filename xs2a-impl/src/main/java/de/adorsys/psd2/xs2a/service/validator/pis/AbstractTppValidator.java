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

import de.adorsys.psd2.xs2a.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.service.validator.tpp.PisTppInfoValidator;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public abstract class AbstractTppValidator<T extends TppInfoProvider> implements BusinessValidator<T> {
    @Setter
    private PisTppInfoValidator pisTppInfoValidator;

    @Override
    public ValidationResult validate(@NotNull T object) {
        ValidationResult tppValidationResult = pisTppInfoValidator.validateTpp(object.getTppInfo());
        if (tppValidationResult.isNotValid()) {
            log.info("bla-bla");
            return tppValidationResult;
        }

        return executeBusinessValidation(object);
    }

    abstract ValidationResult executeBusinessValidation(T object);
}
