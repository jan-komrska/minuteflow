package org.minuteflow.core.impl.factory;

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

import org.minuteflow.core.api.bean.PropertyStateAccessor;
import org.minuteflow.core.api.contract.StateCollection;
import org.springframework.beans.factory.annotation.Autowired;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MinuteFlowStateAccessor<Entity extends Object> extends PropertyStateAccessor<Entity> {
    @Autowired
    private StateCollection stateCollection;

    private String[] patterns = null;

    //

    public MinuteFlowStateAccessor(Class<Entity> entityClass, String... patterns) {
        super(entityClass);
        //
        this.patterns = patterns;
    }

    //

    @PostConstruct
    private void init() {
        setManagedStates(stateCollection.getAllStates(patterns));
    }
}
