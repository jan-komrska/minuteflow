package org.minuteflow.tstapp.multi;

/*-
 * ========================LICENSE_START=================================
 * minuteflow-test-application
 * %%
 * Copyright (C) 2024 Jan Komrska
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */

import java.util.Set;

import org.apache.commons.collections4.SetUtils;
import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.AttributeConverter;

public class SetToStringConverter implements AttributeConverter<Set<String>, String> {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final JavaType TYPE_SET_OF_STRINGS = //
            MAPPER.getTypeFactory().constructCollectionLikeType(Set.class, String.class);

    @Override
    public String convertToDatabaseColumn(Set<String> values) {
        try {
            values = SetUtils.emptyIfNull(values);
            return MAPPER.writeValueAsString(values);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public Set<String> convertToEntityAttribute(String value) {
        try {
            value = StringUtils.defaultString(value);
            return MAPPER.readValue(value, TYPE_SET_OF_STRINGS);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
