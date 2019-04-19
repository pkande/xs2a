/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
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

package de.adorsys.psd2.xs2a.web.interceptor;

import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.web.validator.ErrorBuildingService;
import de.adorsys.psd2.xs2a.web.validator.MethodValidator;
import de.adorsys.psd2.xs2a.web.validator.MethodValidatorController;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class HeaderValidationInterceptor extends HandlerInterceptorAdapter {

    private final ErrorBuildingService errorBuildingService;
    private final MethodValidatorController methodValidatorController;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        return isRequestValid(request, response, handler);
    }

    private boolean isRequestValid(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {

        // This MessageError instance may be enriched in all chains of validation (headers and body) for all methods.
        MessageError initialMessageError = new MessageError();

        // Services for the definite method validations are called by method name via factory pattern here. To add any new
        // validators please include the new class to the 'methods.factory' package and add new enum to ControllerMethodsForValidation.
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            String methodName = handlerMethod.getMethod().getName();

            Optional<MethodValidator> methodValidator = methodValidatorController.getMethod(methodName);
            if (methodValidator.isPresent()) {
                methodValidator.get().validate(request, initialMessageError);

                if (!initialMessageError.getTppMessages().isEmpty()) {
                    // Last part of all validations: if there is at least one error - we build 400 response.
                    errorBuildingService.buildErrorResponse(response, initialMessageError);
                    return false;
                }
            } else {
                return true;
            }
        }
        return true;
    }

}
