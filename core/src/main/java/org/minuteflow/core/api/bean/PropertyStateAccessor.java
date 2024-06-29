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

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.SetUtils;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
import org.apache.commons.lang3.reflect.TypeUtils;
import org.minuteflow.core.api.contract.CalculatedState;
import org.minuteflow.core.api.contract.PropertyEntry;
import org.minuteflow.core.api.contract.PropertyState;
import org.minuteflow.core.api.contract.State;
import org.minuteflow.core.api.exception.EntityUpdateRejectedException;
import org.springframework.beans.PropertyAccessor;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.TypeDescriptor;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PropertyStateAccessor<Entity extends Object> extends BaseStateAccessor<Entity> {
    @Autowired
    private ObjectMapper objectMapper;

    private Set<State> managedStates = null;

    //

    public PropertyStateAccessor(Class<Entity> entityClass) {
        super(entityClass);
    }

    //

    private Type getType(TypeDescriptor typeDescriptor) {
        return typeDescriptor.getResolvableType().getType();
    }

    private Type getType(Type type, TypeVariable<? extends Class<?>> variable) {
        return TypeUtils.getTypeArguments(type, variable.getGenericDeclaration()).get(variable);
    }

    private Object convertValue(Object value, Type targetType) {
        return objectMapper.convertValue(value, objectMapper.constructType(targetType));
    }

    //

    @Override
    protected Set<State> getStatesImpl(Entity entity) {
        Set<State> appliedStates = new HashSet<State>();
        //
        PropertyAccessor entityPropertyAccessor = PropertyAccessorFactory.forBeanPropertyAccess(entity);
        for (State managedState : SetUtils.emptyIfNull(managedStates)) {
            if (managedState instanceof PropertyState propertyState) {
                boolean applied = true;
                //
                for (PropertyEntry stateEntry : propertyState.getProperties().values()) {
                    Type entityValueType = getType(entityPropertyAccessor.getPropertyTypeDescriptor(stateEntry.getKey()));
                    //
                    if (TypeUtils.isAssignable(entityValueType, Collection.class)) {
                        Type entityItemType = getType(entityValueType, Collection.class.getTypeParameters()[0]);
                        Object stateValue = convertValue(stateEntry.getValue(), entityItemType);
                        Collection<?> entityValues = (Collection<?>) entityPropertyAccessor.getPropertyValue(stateEntry.getKey());
                        //
                        applied = applied && entityValues.contains(stateValue);
                    } else {
                        Object stateValue = convertValue(stateEntry.getValue(), entityValueType);
                        Object entityValue = entityPropertyAccessor.getPropertyValue(stateEntry.getKey());
                        //
                        applied = applied && Objects.equals(entityValue, stateValue);
                    }
                }
                //
                if (applied) {
                    appliedStates.add(managedState);
                }
            }
        }
        //
        Set<State> persistentAppliedStates = new HashSet<State>(appliedStates);
        //
        for (State managedState : SetUtils.emptyIfNull(managedStates)) {
            if (managedState instanceof CalculatedState calculatedState) {
                if (calculatedState.appliesTo(persistentAppliedStates)) {
                    appliedStates.add(calculatedState);
                }
            }
        }
        //
        return appliedStates;
    }

    @Override
    protected void setStatesImpl(Entity entity, Set<State> states) throws EntityUpdateRejectedException {
        MultiValuedMap<String, Object> stateProperties = new HashSetValuedHashMap<String, Object>();
        //
        for (State state : SetUtils.emptyIfNull(states)) {
            if (state instanceof PropertyState propertyState) {
                for (PropertyEntry stateEntry : propertyState.getProperties().values()) {
                    stateProperties.put(stateEntry.getKey(), stateEntry.getValue());
                }
            }
        }
        //
        PropertyAccessor entityPropertyAccessor = PropertyAccessorFactory.forBeanPropertyAccess(entity);
        for (String key : stateProperties.keys()) {
            Collection<Object> stateValues = new HashSet<Object>(stateProperties.get(key));
            Type entityValueType = getType(entityPropertyAccessor.getPropertyTypeDescriptor(key));
            //
            if (TypeUtils.isAssignable(entityValueType, Collection.class)) {
                Object entityValues = convertValue(stateValues, entityValueType);
                entityPropertyAccessor.setPropertyValue(key, entityValues);
            } else {
                if (stateValues.size() == 1) {
                    Object entityValue = convertValue(stateValues.iterator().next(), entityValueType);
                    entityPropertyAccessor.setPropertyValue(key, entityValue);
                } else {
                    throw new IllegalStateException();
                }
            }
        }
    }

    public PropertyStateAccessor<Entity> withManagedStates(Set<State> managedStates) {
        setManagedStates(managedStates);
        return this;
    }

    public PropertyStateAccessor<Entity> withManagedStates(State... managedStates) {
        setManagedStates((managedStates != null) ? Set.of(managedStates) : null);
        return this;
    }
}
