package org.minuteflow.tstapp.oop;

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

import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.minuteflow.core.MinuteFlowConfiguration;
import org.minuteflow.core.api.annotation.ActionRef;
import org.minuteflow.core.api.bean.BaseController;
import org.minuteflow.core.api.bean.BaseState;
import org.minuteflow.core.api.bean.DispatchProxyFactory;
import org.minuteflow.core.api.bean.MappedStateAccessor;
import org.minuteflow.core.api.contract.Controller;
import org.minuteflow.core.api.contract.Dispatcher;
import org.minuteflow.core.api.contract.State;
import org.minuteflow.core.api.contract.StateAccessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@Import(MinuteFlowConfiguration.class)
public class AnimalConfiguration {
    @Autowired
    private Dispatcher dispatcher;

    //

    @Primary
    @Bean
    public DispatchProxyFactory<AnimalManager> taskManager() {
        return new DispatchProxyFactory<AnimalManager>(AnimalManager.class, dispatcher);
    }

    @Bean
    public StateAccessor taskStateAccessor() {
        var stateMap = new DualHashBidiMap<AnimalEntityType, State>();
        stateMap.put(AnimalEntityType.DOG, animalStateDog());
        stateMap.put(AnimalEntityType.CAT, animalStateCat());
        //
        var accessor = new MappedStateAccessor<AnimalEntity, AnimalEntityType>(AnimalEntity.class, AnimalEntityType.class);
        accessor.setStateMap(stateMap);
        accessor.setStateGetter(AnimalEntity::getType);
        accessor.setStateSetter(AnimalEntity::setType);
        return accessor;
    }

    //

    @Bean
    public State animalStateDog() {
        return new BaseState();
    }

    @Bean
    public State animalStateCat() {
        return new BaseState();
    }

    //

    @Bean
    public AnimalManager animalManagerStateDog() {
        return new AnimalManager() {
            @Override
            @ActionRef
            public void makeSound(AnimalEntity animal) {
                log.info(animal + " make sound: woof woof.");
            }
        };
    }

    @Bean
    public Controller animalControllerStateDog() {
        return new BaseController(animalStateDog(), animalManagerStateDog());
    }

    @Bean
    public AnimalManager animalManagerStateCat() {
        return new AnimalManager() {
            @Override
            @ActionRef
            public void makeSound(AnimalEntity animal) {
                log.info(animal + " make sound: meow meow.");
            }
        };
    }

    @Bean
    public Controller animalControllerStateCat() {
        return new BaseController(animalStateCat(), animalManagerStateCat());
    }
}
