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

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.minuteflow.core.api.contract.StateAccessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class StateAccessorRepository {
    @Getter
    @Setter
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    private static class StateAccessorId {
        private String groupName = null;
        private Class<?> entityClass = null;
    }

    @Autowired
    private ApplicationContext applicationContext = null;

    private Map<StateAccessorId, String> stateAccessorMap = new ConcurrentHashMap<StateAccessorId, String>();

    //

    public StateAccessorRepository() {
    }

    //

    public StateAccessor getStateAccessor(String groupName, Class<?> entityClass) {
        StateAccessorId stateAccessorId = new StateAccessorId(groupName, entityClass);
        if (stateAccessorMap.containsKey(stateAccessorId)) {
            String beanName = stateAccessorMap.get(stateAccessorId);
            return applicationContext.getBean(beanName, StateAccessor.class);
        } else {
            return null;
        }
    }

    public void addStateAccessor(StateAccessor stateAccessor, String beanName) {
        Objects.requireNonNull(stateAccessor);
        Objects.requireNonNull(beanName);
        //
        StateAccessorId stateAccessorId = new StateAccessorId(stateAccessor.getGroupName(), stateAccessor.getEntityClass());
        String registeredBeanName = stateAccessorMap.putIfAbsent(stateAccessorId, beanName);
        if (Objects.isNull(registeredBeanName)) {
            log.debug("registered state accessor [" + stateAccessorId + "] implemented by [" + beanName + "]");
        } else {
            throw new IllegalStateException();
        }
    }

    public void removeStateAccessor(StateAccessor stateAccessor, String beanName) {
        Objects.requireNonNull(stateAccessor);
        Objects.requireNonNull(beanName);
        //
        StateAccessorId stateAccessorId = new StateAccessorId(stateAccessor.getGroupName(), stateAccessor.getEntityClass());
        boolean removed = stateAccessorMap.remove(stateAccessorId, beanName);
        if (removed) {
            log.debug("unregistered state accessor [" + stateAccessorId + "]");
        }
    }
}
