package org.minuteflow.tstapp.multi;

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
import org.minuteflow.core.api.annotation.ControllerRefType;
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
public class OrderFlowConfiguration {
    @Autowired
    private StateManager stateManager;

    @Autowired
    private Dispatcher dispatcher;

    //

    @Primary
    @Bean
    public DispatchProxyFactory<OrderManager> orderManager() {
        return new DispatchProxyFactory<OrderManager>(OrderManager.class, dispatcher);
    }

    @Bean
    public StateAccessor orderStateAccessor(@Autowired StateCollection stateCollection) {
        return new PropertyStateAccessor<OrderEntity>(OrderEntity.class). //
                withManagedStates(stateCollection.getAllStates("orderState*", "orderManager*"));
    }

    //

    @Bean
    public State orderStateOpen() {
        return new BasePropertyState().withStateNameProperty("states");
    }

    @Bean
    public State orderStateDone() {
        return new BasePropertyState().withStateNameProperty("states");
    }

    @Bean
    public State orderStatePaymentRequested() {
        return new BasePropertyState().withStateNameProperty("states");
    }

    @Bean
    public State orderStatePaymentDone() {
        return new BasePropertyState().withStateNameProperty("states");
    }

    @Bean
    public State orderStatePackagingRequested() {
        return new BasePropertyState().withStateNameProperty("states");
    }

    @Bean
    public State orderStatePackagingDone() {
        return new BasePropertyState().withStateNameProperty("states");
    }

    //

    @ControllerRef("orderStateOpen")
    @Bean
    public OrderManager orderManagerStateOpen() {
        return new OrderManager() {
            @Override
            @ActionRef
            public void startOrder(OrderEntity order) {
                log.info("startOrder: " + order);
                stateManager.updateState(order, //
                        new State[] { orderStateOpen() }, //
                        new State[] { orderStatePaymentRequested(), orderStatePackagingRequested() } //
                );
                log.info("  - updated: " + order);
            }
        };
    }

    @ControllerRef("orderStatePaymentRequested")
    @Bean
    public OrderManager orderManagerStatePaymentRequested() {
        return new OrderManager() {
            @Autowired
            private OrderManager orderManager;

            @Override
            @ActionRef
            public void orderPaymentDone(OrderEntity order) {
                log.info("orderPaymentDone: " + order);
                stateManager.updateState(order, orderStatePaymentRequested(), orderStatePaymentDone());
                log.info("  - updated: " + order);
                //
                orderManager.finishOrder(order);
            }
        };
    }

    @ControllerRef("orderStatePackagingRequested")
    @Bean
    public OrderManager orderManagerStatePackagingRequested() {
        return new OrderManager() {
            @Autowired
            private OrderManager orderManager;

            @Override
            @ActionRef
            public void orderPackagingDone(OrderEntity order) {
                log.info("orderPackagingDone: " + order);
                stateManager.updateState(order, orderStatePackagingRequested(), orderStatePackagingDone());
                log.info("  - updated: " + order);
                //
                orderManager.finishOrder(order);
            }
        };
    }

    @ControllerRef(type = ControllerRefType.AND, value = { "orderStatePaymentDone", "orderStatePackagingDone" })
    @Bean
    public OrderManager orderManagerStatePaPDone() {
        return new OrderManager() {
            @Override
            @ActionRef
            public void finishOrder(OrderEntity order) {
                log.info("finishOrder: " + order);
                stateManager.updateState(order, //
                        new State[] { orderStatePaymentDone(), orderStatePackagingDone() }, //
                        new State[] { orderStateDone() } //
                );
                log.info("  - updated: " + order);
            }
        };
    }

    @ControllerRef(type = ControllerRefType.NAND, value = { "orderStatePaymentDone", "orderStatePackagingDone" })
    @Bean
    public OrderManager orderManagerStatePaPNotDone() {
        return new OrderManager() {
            @Override
            @ActionRef
            public void finishOrder(OrderEntity order) {
                log.info("(ignore) finishOrder: " + order);
            }
        };
    }
}
