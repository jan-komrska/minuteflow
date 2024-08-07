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

import org.minuteflow.core.MinuteFlowConfiguration;
import org.minuteflow.core.api.annotation.ActionRef;
import org.minuteflow.core.api.annotation.ControllerRef;
import org.minuteflow.core.api.annotation.MinuteEntityRef;
import org.minuteflow.core.api.annotation.MinuteServiceRef;
import org.minuteflow.core.api.bean.BasePropertyState;
import org.minuteflow.core.api.bean.BaseState;
import org.minuteflow.core.api.contract.State;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@Import(MinuteFlowConfiguration.class)
@MinuteServiceRef(serviceClass = AnimalManager.class)
@MinuteEntityRef(entityClass = AnimalEntity.class, statePattern = "animalState*")
public class AnimalConfiguration {
    @Bean
    public State animalStateMammal() {
        return new BaseState();
    }

    @Bean
    public State animalStateDog() {
        return new BasePropertyState(animalStateMammal()).withProperty("type", AnimalEntityType.DOG);
    }

    @Bean
    public State animalStateCat() {
        return new BasePropertyState(animalStateMammal()).withProperty("type", AnimalEntityType.CAT);
    }

    //

    @ControllerRef("animalStateMammal")
    @Bean
    public AnimalManager animalManagerStateMammal() {
        return new AnimalManager() {
            @Override
            @ActionRef
            public void move(AnimalEntity animal) {
                log.info(animal + " is moving.");
            }
        };
    }

    @ControllerRef("animalStateDog")
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

    @ControllerRef("animalStateCat")
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
}
