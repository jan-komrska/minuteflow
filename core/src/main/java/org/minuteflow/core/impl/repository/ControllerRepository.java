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

import org.minuteflow.core.api.contract.Controller;
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
public class ControllerRepository {
    @Getter
    @Setter
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    private static class ActionId {
        private String stateName = null;
        private String actionName = null;
    }

    //

    @Autowired
    private ApplicationContext applicationContext = null;

    private Map<ActionId, String> actionMap = new ConcurrentHashMap<ActionId, String>();

    //

    public ControllerRepository() {
    }

    //

    public Controller getController(String stateName, String actionName) {
        ActionId actionId = new ActionId(stateName, actionName);
        if (actionMap.containsKey(actionId)) {
            String beanName = actionMap.get(actionId);
            return applicationContext.getBean(beanName, Controller.class);
        } else {
            return null;
        }
    }

    public void addController(Controller controller, String beanName) {
        Objects.requireNonNull(controller);
        Objects.requireNonNull(beanName);
        //
        String stateName = controller.getParentState().getName();
        for (String actionName : controller.getActionNames()) {
            ActionId actionId = new ActionId(stateName, actionName);
            String registeredBeanName = actionMap.putIfAbsent(actionId, beanName);
            if (Objects.isNull(registeredBeanName)) {
                log.debug("registered action [" + actionId + "] for controller [" + beanName + "]");
            } else {
                throw new IllegalStateException();
            }
        }
    }

    public void removeController(Controller controller, String beanName) {
        Objects.requireNonNull(controller);
        Objects.requireNonNull(beanName);
        //
        String stateName = controller.getParentState().getName();
        for (String actionName : controller.getActionNames()) {
            ActionId actionId = new ActionId(stateName, actionName);
            boolean removed = actionMap.remove(actionId, beanName);
            if (removed) {
                log.debug("unregistered action [" + actionId + "]");
            }
        }
    }
}
