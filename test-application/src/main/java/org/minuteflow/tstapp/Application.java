package org.minuteflow.tstapp;

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

import java.util.Set;

import org.minuteflow.core.api.contract.Source;
import org.minuteflow.core.api.contract.State;
import org.minuteflow.core.api.contract.StateManager;
import org.minuteflow.tstapp.multi.OrderEntity;
import org.minuteflow.tstapp.multi.OrderEntityRepository;
import org.minuteflow.tstapp.multi.OrderManager;
import org.minuteflow.tstapp.oop.AnimalEntity;
import org.minuteflow.tstapp.oop.AnimalEntityType;
import org.minuteflow.tstapp.oop.AnimalManager;
import org.minuteflow.tstapp.simple.TaskEntity;
import org.minuteflow.tstapp.simple.TaskEntityState;
import org.minuteflow.tstapp.simple.TaskManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Application {
    @Autowired
    private TaskManager taskManager;

    public void taskManagerExample() {
        TaskEntity taskEntity = new TaskEntity();
        taskEntity.setId(1l);
        taskEntity.setName("Prepare project");
        taskEntity.setState(TaskEntityState.OPEN);
        //
        taskManager.startTask(taskEntity);
        taskManager.finishTask(taskEntity);
    }

    //

    @Autowired
    private OrderEntityRepository orderEntityRepository;

    @Autowired
    private OrderManager orderManager;

    @Autowired
    private StateManager stateManager;

    @Autowired
    @Qualifier("orderStateOpen")
    private State orderStateOpen;

    public void orderManagerExample() {
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setId(2L);
        orderEntity.setName("Order lunch");
        stateManager.setStates(orderEntity, Set.of(orderStateOpen));
        //
        orderEntityRepository.save(orderEntity);
        //
        orderManager.startOrder(Source.withParameters("findById", orderEntity.getId()));
        orderManager.orderPaymentDone(Source.withParameters("findById", orderEntity.getId()));
        orderManager.orderPackagingDone(Source.withParameters("findById", orderEntity.getId()));
    }

    //

    @Autowired
    private AnimalManager animalManager;

    public void animalManagerExample() {
        AnimalEntity dog = new AnimalEntity();
        dog.setId(1l);
        dog.setName("Max");
        dog.setType(AnimalEntityType.DOG);
        //
        AnimalEntity cat = new AnimalEntity();
        cat.setId(2l);
        cat.setName("Molly");
        cat.setType(AnimalEntityType.CAT);
        //
        animalManager.move(dog);
        animalManager.makeSound(dog);
        animalManager.move(cat);
        animalManager.makeSound(cat);
    }

    //

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner() {
        return args -> {
            System.out.println("Let's start flow:");
            System.out.println("--");
            animalManagerExample();
            System.out.println("--");
            taskManagerExample();
            System.out.println("--");
            orderManagerExample();
            System.out.println("--");
        };
    }
}
