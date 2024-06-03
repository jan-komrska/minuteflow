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

import org.minuteflow.core.api.contract.Controller;
import org.minuteflow.core.api.contract.State;
import org.minuteflow.core.api.contract.StateAccessor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.DestructionAwareBeanPostProcessor;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

@Component
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class RepositoryObserver implements DestructionAwareBeanPostProcessor, Ordered {
    @Lazy
    @Autowired
    private StateRepository stateRepository;

    @Lazy
    @Autowired
    private ControllerRepository controllerRepository;

    @Lazy
    @Autowired
    private StateAccessorRepository stateAccessorRepository;

    //

    public RepositoryObserver() {
    }

    //

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof State state) {
            stateRepository.addState(state, beanName);
        }
        if (bean instanceof Controller controller) {
            controllerRepository.addController(controller, beanName);
        }
        if (bean instanceof StateAccessor stateAccessor) {
            stateAccessorRepository.addStateAccessor(stateAccessor, beanName);
        }
        //
        return bean;
    }

    @Override
    public void postProcessBeforeDestruction(Object bean, String beanName) throws BeansException {
        if (bean instanceof State state) {
            stateRepository.removeState(state, beanName);
        }
        if (bean instanceof Controller controller) {
            controllerRepository.removeController(controller, beanName);
        }
        if (bean instanceof StateAccessor stateAccessor) {
            stateAccessorRepository.removeStateAccessor(stateAccessor, beanName);
        }
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
