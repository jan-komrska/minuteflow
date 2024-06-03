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

import org.apache.commons.lang3.StringUtils;
import org.minuteflow.core.api.contract.State;
import org.springframework.beans.factory.BeanNameAware;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class BaseState implements State, BeanNameAware {
    @Setter
    private String name = null;

    @Getter
    @Setter
    private State parentState = null;

    //

    public BaseState() {
    }

    public BaseState(String name) {
        this.name = name;
    }

    public BaseState(String name, State parentState) {
        this.name = name;
        this.parentState = parentState;
    }

    //

    @ToString.Include
    @EqualsAndHashCode.Include
    public String getName() {
        return StringUtils.defaultString(name);
    }

    @Override
    public void setBeanName(String beanName) {
        if (getName().isEmpty()) {
            setName(beanName);
        }
    }
}
