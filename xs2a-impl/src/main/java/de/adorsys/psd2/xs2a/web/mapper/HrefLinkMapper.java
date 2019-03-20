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

package de.adorsys.psd2.xs2a.web.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.adorsys.psd2.xs2a.domain.Links;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class HrefLinkMapper {
    private final ObjectMapper mapper;

    /**
     * Maps Links to HrefType Links Map
     *
     * @param links Links model Object, where URI links stored as a string
     * @return Map with link name and href value.
     * Returned Map with added 'href' to link value.
     */
    public Map mapToLinksMap(Links links) {
        return mapper.convertValue(links, Map.class);
    }


    public Map mapToLinksMap(String name, String link) {
        return Collections.singletonMap(name, link);
    }
}
