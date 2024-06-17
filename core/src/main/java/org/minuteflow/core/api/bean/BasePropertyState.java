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

import org.apache.commons.collections4.MapUtils;
import org.minuteflow.core.api.contract.State;

public class BasePropertyState extends BaseState {
    private Map<String, Object> properties = Collections.emptyMap();

    //

    public BasePropertyState() {
        super();
    }

    public BasePropertyState(String name) {
        super(name);
    }

    public BasePropertyState(String name, State parentState) {
        super(name, parentState);
    }

    //

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        properties = new HashMap<String, Object>(MapUtils.emptyIfNull(properties));
        this.properties = Collections.unmodifiableMap(properties);
    }
}
