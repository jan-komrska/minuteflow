package org.minuteflow.tstapp.simple;

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
import org.minuteflow.core.api.bean.BasePropertyState;
import org.minuteflow.core.api.bean.DispatchProxyFactory;
import org.minuteflow.core.api.bean.PropertyStateAccessor;
import org.minuteflow.core.api.contract.Dispatcher;
import org.minuteflow.core.api.contract.State;
import org.minuteflow.core.api.contract.StateAccessor;
import org.minuteflow.core.api.contract.StateCollection;
import org.minuteflow.core.api.contract.StateManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@Import(MinuteFlowConfiguration.class)
public class TaskFlowConfiguration {
    @Bean
    public State taskStateOpen() {
        return new BasePropertyState().withProperty("state", TaskEntityState.OPEN);
    }

    @Bean
    public State taskStateInProgress() {
        return new BasePropertyState().withProperty("state", TaskEntityState.IN_PROGRESS);
    }

    @Bean
    public State taskStateDone() {
        return new BasePropertyState().withProperty("state", TaskEntityState.DONE);
    }

    @Bean
    public StateAccessor taskStateAccessor(@Autowired StateCollection stateCollection) {
        return new PropertyStateAccessor<TaskEntity>(TaskEntity.class). //
                withManagedStates(stateCollection.getAllStates("taskState*"));
    }

    //

    @ControllerRef("taskStateOpen")
    @Bean
    public TaskManager taskManagerStateOpen(@Autowired StateManager stateManager) {
        return new TaskManager() {
            @Override
            @ActionRef
            public void startTask(TaskEntity task) {
                log.info("startTask: " + task);
                stateManager.updateState(task, taskStateOpen(), taskStateInProgress());
                log.info("  - updated: " + task);
            }
        };
    }

    @ControllerRef("taskStateInProgress")
    @Bean
    public TaskManager taskManagerStateInProgress(@Autowired StateManager stateManager) {
        return new TaskManager() {
            @Override
            @ActionRef
            public void finishTask(TaskEntity task) {
                log.info("finishTask: " + task);
                stateManager.updateState(task, taskStateInProgress(), taskStateDone());
                log.info("  - updated: " + task);
            }
        };
    }

    @Primary
    @Bean
    public DispatchProxyFactory<TaskManager> taskManager(@Autowired Dispatcher dispatcher) {
        return new DispatchProxyFactory<TaskManager>(TaskManager.class, dispatcher);
    }
}
