package org.minuteflow.core.api.bean;

/*-
 * ========================LICENSE_START=================================
 * minuteflow-core
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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.minuteflow.core.api.contract.PropertyEntry;
import org.minuteflow.core.api.contract.PropertyState;
import org.minuteflow.core.api.contract.State;

import lombok.AllArgsConstructor;
import lombok.Data;

public class BasePropertyState extends BaseState implements PropertyState {
    @AllArgsConstructor
    @Data
    private class ValuePropertyEntry implements PropertyEntry {
        private String key = null;
        private Object value = null;
    }

    @AllArgsConstructor
    @Data
    private class StateNamePropertyEntry implements PropertyEntry {
        private String key = null;

        @Override
        public Object getValue() {
            return BasePropertyState.this.getName();
        }
    }

    //

    private Map<String, PropertyEntry> rwProperties;
    private Map<String, PropertyEntry> roProperties;

    {
        rwProperties = new HashMap<String, PropertyEntry>();
        roProperties = Collections.unmodifiableMap(rwProperties);
    }

    //

    public BasePropertyState() {
        super();
    }

    public BasePropertyState(State parentState) {
        super(parentState);
    }

    //

    public Map<String, PropertyEntry> getProperties() {
        return roProperties;
    }

    public BasePropertyState withProperty(String key, Object value) {
        rwProperties.put(key, new ValuePropertyEntry(key, value));
        return this;
    }

    public BasePropertyState withStateNameProperty(String key) {
        rwProperties.put(key, new StateNamePropertyEntry(key));
        return this;
    }
}
