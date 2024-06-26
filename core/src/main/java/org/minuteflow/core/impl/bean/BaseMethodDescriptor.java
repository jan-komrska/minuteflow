package org.minuteflow.core.impl.bean;

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

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.ArrayUtils;
import org.minuteflow.core.api.annotation.EntityRef;
import org.minuteflow.core.api.contract.MethodDescriptor;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class BaseMethodDescriptor implements MethodDescriptor {
    @Getter
    private static class ActionNameAccessor {
        private Method method = null;
        private String actionName = null;

        public ActionNameAccessor(Method method) {
            this.method = method;
            this.actionName = method.getDeclaringClass().getName() + "." + method.getName();
        }
    }

    @Getter
    private static class EntityAccessor {
        private Method method = null;
        private int entityIndex = -1;

        public EntityAccessor(Method method) {
            this.method = method;
            this.entityIndex = -1;
            //
            Parameter[] parameters = ArrayUtils.nullToEmpty(method.getParameters(), Parameter[].class);
            for (int index = 0; index < parameters.length; index++) {
                Parameter parameter = parameters[index];
                if (parameter.getAnnotation(EntityRef.class) != null) {
                    entityIndex = index;
                }
            }
        }

        public Object getEntity(Object[] args) {
            args = ArrayUtils.nullToEmpty(args);
            //
            if ((0 <= entityIndex) && (entityIndex < args.length)) {
                return args[entityIndex];
            } else {
                return null;
            }
        }
    }

    //

    private Map<Method, ActionNameAccessor> actionNameAccessorMap = new ConcurrentHashMap<Method, ActionNameAccessor>();
    private Map<Method, EntityAccessor> entityAccessorMap = new ConcurrentHashMap<Method, EntityAccessor>();

    //

    public String getActionName(Method method) {
        if (!actionNameAccessorMap.containsKey(method)) {
            ActionNameAccessor actionNameAccessor = new ActionNameAccessor(method);
            actionNameAccessorMap.putIfAbsent(method, actionNameAccessor);
            log.debug("registered actionNameAccessor for method: " + method.getDeclaringClass().getName() + "." + method.getName());
        }
        return actionNameAccessorMap.get(method).getActionName();
    }

    public Object getEntity(Method method, Object[] args) {
        if (!entityAccessorMap.containsKey(method)) {
            EntityAccessor entityAccessor = new EntityAccessor(method);
            entityAccessorMap.putIfAbsent(method, entityAccessor);
            log.debug("registered entityAccessor for method: " + method.getDeclaringClass().getName() + "." + method.getName());
        }
        return entityAccessorMap.get(method).getEntity(args);
    }
}
