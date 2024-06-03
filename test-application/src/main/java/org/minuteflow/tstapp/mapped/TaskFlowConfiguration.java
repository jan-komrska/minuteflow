package org.minuteflow.tstapp.mapped;

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
    @Autowired
    private StateManager stateManager;

    @Autowired
    private Dispatcher dispatcher;

    //

    @Primary
    @Bean
    public DispatchProxyFactory<TaskManager> taskManager() {
        return new DispatchProxyFactory<TaskManager>(TaskManager.class, dispatcher);
    }

    @Bean
    public StateAccessor taskStateAccessor() {
        var stateMap = new DualHashBidiMap<TaskEntityState, State>();
        stateMap.put(TaskEntityState.OPEN, taskStateOpen());
        stateMap.put(TaskEntityState.IN_PROGRESS, taskStateInProgress());
        stateMap.put(TaskEntityState.DONE, taskStateDone());
        //
        var accessor = new MappedStateAccessor<TaskEntity, TaskEntityState>(TaskEntity.class, TaskEntityState.class);
        accessor.setStateMap(stateMap);
        accessor.setStateGetter(TaskEntity::getState);
        accessor.setStateSetter(TaskEntity::setState);
        return accessor;
    }

    //

    @Bean
    public State taskStateOpen() {
        return new BaseState();
    }

    @Bean
    public State taskStateInProgress() {
        return new BaseState();
    }

    @Bean
    public State taskStateDone() {
        return new BaseState();
    }

    //

    @Bean
    public TaskManager taskManagerStateOpen() {
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

    @Bean
    public Controller taskControllerStateOpen() {
        return new BaseController(taskStateOpen(), taskManagerStateOpen());
    }

    //

    @Bean
    public TaskManager taskManagerStateInProgress() {
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

    @Bean
    public Controller taskControllerStateInProgress(StateManager stateManager) {
        return new BaseController(taskStateInProgress(), taskManagerStateInProgress());
    }
}
