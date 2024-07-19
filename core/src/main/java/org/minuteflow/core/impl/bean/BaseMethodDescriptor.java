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
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.reflect.TypeUtils;
import org.minuteflow.core.api.annotation.EntityRef;
import org.minuteflow.core.api.annotation.NamedRef;
import org.minuteflow.core.api.contract.MethodDescriptor;
import org.minuteflow.core.api.contract.Source;
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
        private boolean staticAction = true;
        private String entityName = null;
        private Class<?> entityClass = null;

        public EntityAccessor(Method method) {
            this.method = method;
            this.entityIndex = -1;
            this.staticAction = true;
            //
            Parameter[] parameters = ArrayUtils.nullToEmpty(this.method.getParameters(), Parameter[].class);
            for (int index = 0; index < parameters.length; index++) {
                Parameter parameter = parameters[index];
                EntityRef entityRef = parameter.getAnnotation(EntityRef.class);
                NamedRef namedRef = parameter.getAnnotation(NamedRef.class);
                if (entityRef != null) {
                    this.entityIndex = index;
                    this.staticAction = false;
                    this.entityName = (namedRef != null) ? namedRef.value() : null;
                    this.entityClass = entityRef.value();
                    //
                    if (TypeUtils.isAssignable(this.entityClass, Void.class)) {
                        Type entityType = parameter.getParameterizedType();
                        if (TypeUtils.isAssignable(entityType, Source.class)) {
                            entityType = getType(entityType, Source.class.getTypeParameters()[0]);
                        }
                        //
                        this.entityClass = (entityType instanceof Class<?> value) ? value : null;
                    }
                }
            }
        }

        private Type getType(Type type, TypeVariable<? extends Class<?>> variable) {
            return TypeUtils.getTypeArguments(type, variable.getGenericDeclaration()).get(variable);
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

        public void setEntity(Object[] args, Object entity) {
            args = ArrayUtils.nullToEmpty(args);
            //
            if ((0 <= entityIndex) && (entityIndex < args.length)) {
                args[entityIndex] = entity;
            } else {
                throw new IllegalStateException();
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

    public boolean isStaticAction(Method method) {
        if (!entityAccessorMap.containsKey(method)) {
            EntityAccessor entityAccessor = new EntityAccessor(method);
            entityAccessorMap.putIfAbsent(method, entityAccessor);
            log.debug("registered entityAccessor for method: " + method.getDeclaringClass().getName() + "." + method.getName());
        }
        return entityAccessorMap.get(method).isStaticAction();
    }

    public Object getEntity(Method method, Object[] args) {
        if (!entityAccessorMap.containsKey(method)) {
            EntityAccessor entityAccessor = new EntityAccessor(method);
            entityAccessorMap.putIfAbsent(method, entityAccessor);
            log.debug("registered entityAccessor for method: " + method.getDeclaringClass().getName() + "." + method.getName());
        }
        return entityAccessorMap.get(method).getEntity(args);
    }

    public String getEntityName(Method method) {
        if (!entityAccessorMap.containsKey(method)) {
            EntityAccessor entityAccessor = new EntityAccessor(method);
            entityAccessorMap.putIfAbsent(method, entityAccessor);
            log.debug("registered entityAccessor for method: " + method.getDeclaringClass().getName() + "." + method.getName());
        }
        return entityAccessorMap.get(method).getEntityName();
    }

    public Class<?> getEntityClass(Method method) {
        if (!entityAccessorMap.containsKey(method)) {
            EntityAccessor entityAccessor = new EntityAccessor(method);
            entityAccessorMap.putIfAbsent(method, entityAccessor);
            log.debug("registered entityAccessor for method: " + method.getDeclaringClass().getName() + "." + method.getName());
        }
        return entityAccessorMap.get(method).getEntityClass();
    }

    public void setEntity(Method method, Object[] args, Object entity) {
        if (!entityAccessorMap.containsKey(method)) {
            EntityAccessor entityAccessor = new EntityAccessor(method);
            entityAccessorMap.putIfAbsent(method, entityAccessor);
            log.debug("registered entityAccessor for method: " + method.getDeclaringClass().getName() + "." + method.getName());
        }
        entityAccessorMap.get(method).setEntity(args, entity);
    }
}
