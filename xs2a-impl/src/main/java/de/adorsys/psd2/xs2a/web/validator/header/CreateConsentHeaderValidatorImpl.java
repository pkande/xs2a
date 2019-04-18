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
import org.apache.commons.lang3.BooleanUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Objects;

import static de.adorsys.psd2.xs2a.web.validator.constants.Xs2aHeaderConstant.TPP_EXPLICIT_AUTHORISATION_PREFERRED;
import static de.adorsys.psd2.xs2a.web.validator.constants.Xs2aHeaderConstant.TPP_REDIRECT_PREFERRED;

public class CreateConsentHeaderValidatorImpl extends AbstractHeaderValidatorImpl {

    public CreateConsentHeaderValidatorImpl(ErrorBuildingService errorBuildingService) {
        super(errorBuildingService);
    }

    @Override
    protected String getHeaderName() {
        return null;
    }

    @Override
    public void validate(Map<String, String> headers, HttpServletRequest request, MessageError messageError) {
        validateTppRedirectPreferred(request, messageError);
        validateTppExplicitAuthorisationPreferred(request, messageError);
    }

    private void validateTppRedirectPreferred(HttpServletRequest request, MessageError messageError) {
        String tppRedirectPreferred = request.getHeader(TPP_REDIRECT_PREFERRED);

        if (Objects.nonNull(tppRedirectPreferred)) {
            Boolean checker = BooleanUtils.toBooleanObject(tppRedirectPreferred);
            if (checker == null) {
                errorBuildingService.enrichMessageError(messageError, "Wrong format for 'TPP-Redirect-Preferred': value should be a boolean");
            }
        }
    }

    private void validateTppExplicitAuthorisationPreferred(HttpServletRequest request, MessageError messageError) {
        String tppExplicitAuthorisationPreferred = request.getHeader(TPP_EXPLICIT_AUTHORISATION_PREFERRED);

        if (Objects.nonNull(tppExplicitAuthorisationPreferred)) {
            Boolean checker = BooleanUtils.toBooleanObject(tppExplicitAuthorisationPreferred);
            if (checker == null) {
                errorBuildingService.enrichMessageError(messageError, "Wrong format for 'TPP-Explicit-Authorisation-Preferred': value should be a boolean");
            }
        }
    }
}
