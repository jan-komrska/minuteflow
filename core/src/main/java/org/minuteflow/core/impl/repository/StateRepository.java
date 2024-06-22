package org.minuteflow.core.impl.repository;

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

import java.util.Objects;

import org.minuteflow.core.api.contract.State;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class StateRepository {
    @Autowired
    private ApplicationContext applicationContext = null;

    //

    public StateRepository() {
    }

    //

    public State getState(String stateName) {
        if (applicationContext.containsBean(stateName)) {
            return applicationContext.getBean(stateName, State.class);
        } else {
            return null;
        }
    }

    public void addState(State state, String beanName) {
        Objects.requireNonNull(state);
        Objects.requireNonNull(beanName);
        //
        if (Objects.equals(state.getName(), beanName)) {
            log.debug("registered state [" + state.getName() + "] implemenented by [" + beanName + "]");
        } else {
            throw new IllegalStateException();
        }
    }

    public void removeState(State state, String beanName) {
        Objects.requireNonNull(state);
        Objects.requireNonNull(beanName);
        log.debug("unregistered state [" + state.getName() + "]");
    }
}
