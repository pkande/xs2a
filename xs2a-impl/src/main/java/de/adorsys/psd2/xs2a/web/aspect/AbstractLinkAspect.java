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

package de.adorsys.psd2.xs2a.web.aspect;

import de.adorsys.psd2.aspsp.profile.service.AspspProfileService;
import de.adorsys.psd2.xs2a.domain.Links;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.service.InitialScaApproachResolver;
import de.adorsys.psd2.xs2a.service.message.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.lang.reflect.ParameterizedType;
import java.util.Collections;
import java.util.Optional;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.web.util.UriComponentsBuilder.fromHttpUrl;

@Slf4j
@Component
@RequiredArgsConstructor
public abstract class AbstractLinkAspect<T> {
    protected final InitialScaApproachResolver scaApproachResolver;
    private final MessageService messageService;
    private final AspspProfileService aspspProfileService;

    protected <B> boolean hasError(ResponseEntity<B> target) {
        Optional<B> body = Optional.ofNullable(target.getBody());
        return body.isPresent() && body.get().getClass()
                                       .isAssignableFrom(MessageError.class);
    }

    <R> ResponseObject<R> enrichErrorTextMessage(ResponseObject<R> response) {
        MessageError error = response.getError();
        TppMessageInformation tppMessage = error.getTppMessage();
        if (StringUtils.isBlank(tppMessage.getText())) {
            tppMessage.setText(messageService.getMessage(tppMessage.getMessageErrorCode().name()));
            error.setTppMessages(Collections.singleton(tppMessage));
        }
        return ResponseObject.<R>builder()
                   .fail(error)
                   .build();
    }

    String buildPath(String path, Object... params) {
        UriComponentsBuilder uriComponentsBuilder = aspspProfileService.getAspspSettings().isForceXs2aBaseUrl()
                                                        ? fromHttpUrl(aspspProfileService.getAspspSettings().getXs2aBaseUrl())
                                                        : fromHttpUrl(linkTo(getControllerClass()).toString());
        return uriComponentsBuilder
                   .path(path)
                   .buildAndExpand(params)
                   .toUriString();
    }

    @SuppressWarnings("unchecked")
    private Class<T> getControllerClass() {
        try {
            String className = ((ParameterizedType) this.getClass().getGenericSuperclass())
                                   .getActualTypeArguments()[0]
                                   .getTypeName();
            return (Class<T>) Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Class isn't parametrized with generic type! Use <>");
        }
    }

    Links buildDefaultPaymentLinks(String paymentService, String paymentProduct, String paymentId) {
        Links links = new Links();
        links.setSelf(buildPath(UrlHolder.PAYMENT_LINK_URL, paymentService, paymentProduct, paymentId));
        links.setStatus(buildPath(UrlHolder.PAYMENT_STATUS_URL, paymentService, paymentProduct, paymentId));
        return links;
    }
}
