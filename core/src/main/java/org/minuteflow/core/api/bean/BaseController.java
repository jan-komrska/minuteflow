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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ClassUtils.Interfaces;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.minuteflow.core.api.annotation.ActionRef;
import org.minuteflow.core.api.contract.Controller;
import org.minuteflow.core.api.contract.MethodDescriptor;
import org.minuteflow.core.api.contract.State;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.factory.annotation.Autowired;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString(onlyExplicitlyIncluded = true)
public class BaseController implements Controller {
    @Getter
    private static class ProxyAction {
        private Method method;
        private MethodHandle methodHandle;

        public ProxyAction(Method method) {
            this.method = method;
            //
            try {
                method.setAccessible(true);
                methodHandle = MethodHandles.lookup().unreflect(method);
            } catch (IllegalAccessException ex) {
                throw new IllegalStateException(ex);
            }
        }

        public Object execute(Object service, Object[] args) throws Throwable {
            Object[] allArgs = ArrayUtils.addFirst(args, service);
            return methodHandle.invokeWithArguments(allArgs);
        }
    }

    //

    @Autowired
    private MethodDescriptor methodDescriptor;

    @Getter
    @Setter
    @ToString.Include
    private State parentState;

    @Getter
    @Setter
    private Object service;

    private Map<String, ProxyAction> actionMap = new HashMap<String, ProxyAction>();

    //

    public BaseController(State parentState, Object service) {
        this.parentState = parentState;
        this.service = service;
    }

    public BaseController(Object service) {
        this.service = service;
    }

    public BaseController() {
    }

    @PostConstruct
    private void init() {
        Class<?> serviceClass = AopProxyUtils.ultimateTargetClass(service);
        //
        List<Method> annotatedMethods = MethodUtils.getMethodsListWithAnnotation(serviceClass, ActionRef.class, true, true);
        for (Method annotatedMethod : annotatedMethods) {
            ProxyAction action = new ProxyAction(annotatedMethod);
            //
            Set<Method> methods = MethodUtils.getOverrideHierarchy(annotatedMethod, Interfaces.INCLUDE);
            for (Method method : methods) {
                String actionName = methodDescriptor.getActionName(method);
                actionMap.putIfAbsent(actionName, action);
            }
        }
    }

    //

    @Override
    @ToString.Include
    public Set<String> getActionNames() {
        return actionMap.keySet();
    }

    @Override
    public Object executeAction(String actionName, Object[] args) throws Throwable {
        if (actionMap.containsKey(actionName)) {
            return actionMap.get(actionName).execute(service, args);
        } else {
            throw new IllegalArgumentException();
        }
    }
}
