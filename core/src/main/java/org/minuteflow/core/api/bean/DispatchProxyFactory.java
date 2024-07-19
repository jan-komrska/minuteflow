package org.minuteflow.core.api.bean;

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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.minuteflow.core.api.contract.DispatchContext;
import org.minuteflow.core.api.contract.Dispatcher;
import org.minuteflow.core.api.contract.State;
import org.springframework.beans.factory.config.AbstractFactoryBean;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class DispatchProxyFactory<Contract> extends AbstractFactoryBean<Contract> {
    @Setter(AccessLevel.NONE)
    private Class<Contract> contract = null;

    @ToString.Exclude
    private Dispatcher dispatcher = null;

    private State staticState = null;

    //

    public DispatchProxyFactory(Class<Contract> contract) {
        this.contract = contract;
    }

    //

    @Override
    public Class<?> getObjectType() {
        return contract;
    }

    @Override
    protected Contract createInstance() throws Exception {
        return contract.cast(Proxy.newProxyInstance( //
                Thread.currentThread().getContextClassLoader(), new Class[] { contract }, //
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        DispatchContext dispatchContext = new DispatchContext();
                        dispatchContext.setStaticState(staticState);
                        //
                        return dispatcher.dispatch(method, args, dispatchContext);
                    }
                }));
    }
}
