package org.minuteflow.tstapp;

import java.util.Set;

import org.minuteflow.core.api.contract.State;
import org.minuteflow.core.api.contract.StateManager;
import org.minuteflow.tstapp.json.OrderEntity;
import org.minuteflow.tstapp.json.OrderManager;

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

import org.minuteflow.tstapp.mapped.TaskEntity;
import org.minuteflow.tstapp.mapped.TaskEntityState;
import org.minuteflow.tstapp.mapped.TaskManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner( //
            @Autowired ApplicationContext context, @Autowired StateManager stateManager, //
            @Autowired @Qualifier("orderStateOpen") State orderStateOpen //
    ) {
        return args -> {
            System.out.println("Let's start flow:");
            //
            TaskEntity taskEntity = new TaskEntity();
            taskEntity.setId(1l);
            taskEntity.setName("Prepare project");
            taskEntity.setState(TaskEntityState.OPEN);
            //
            TaskManager taskManager = context.getBean(TaskManager.class);
            taskManager.startTask(taskEntity);
            taskManager.finishTask(taskEntity);
            //
            OrderEntity orderEntity = new OrderEntity();
            orderEntity.setId(2L);
            orderEntity.setName("Order lunch");
            stateManager.setStates(orderEntity, Set.of(orderStateOpen));
            //
            OrderManager orderManager = context.getBean(OrderManager.class);
            orderManager.startOrder(orderEntity);
            orderManager.orderPaymentDone(orderEntity);
            orderManager.orderPackagingDone(orderEntity);
        };
    }
}
