package org.minuteflow.core.api.bean;

import java.lang.reflect.Type;

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

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.SetUtils;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
import org.minuteflow.core.api.contract.CalculatedState;
import org.minuteflow.core.api.contract.PropertyEntry;
import org.minuteflow.core.api.contract.PropertyState;
import org.minuteflow.core.api.contract.State;
import org.minuteflow.core.api.exception.EntityUpdateRejectedException;
import org.springframework.beans.PropertyAccessor;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.TypeDescriptor;

import com.fasterxml.jackson.databind.JavaType;
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

    private JavaType getType(TypeDescriptor typeDescriptor) {
        Type type = typeDescriptor.getResolvableType().getType();
        return objectMapper.constructType(type);
    }

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
                    JavaType entityValueType = getType(entityPropertyAccessor.getPropertyTypeDescriptor(stateEntry.getKey()));
                    Object entityValue = entityPropertyAccessor.getPropertyValue(stateEntry.getKey());
                    if (entityValueType.isTypeOrSubTypeOf(Collection.class)) {
                        Collection<?> entityValueAsCollection = (Collection<?>) entityValue;
                        applied = applied && entityValueAsCollection.contains(stateEntry.getValue());
                    } else {
                        applied = applied && Objects.equals(entityValue, stateEntry.getValue());
                    }
                }
                //
                if (applied) {
                    appliedStates.add(managedState);
                }
            }
        }
        //
        for (State managedState : SetUtils.emptyIfNull(managedStates)) {
            if (managedState instanceof CalculatedState calculatedState) {
                appliedStates.add(calculatedState);
            }
        }
        //
        return appliedStates;
    }

    @Override
    protected void setStatesImpl(Entity entity, Set<State> states) throws EntityUpdateRejectedException {
        MultiValuedMap<String, Object> entityProperties = new HashSetValuedHashMap<String, Object>();
        //
        for (State state : SetUtils.emptyIfNull(states)) {
            if (state instanceof PropertyState propertyState) {
                for (PropertyEntry stateEntry : propertyState.getProperties().values()) {
                    entityProperties.put(stateEntry.getKey(), stateEntry.getValue());
                }
            }
        }
        //
        PropertyAccessor entityPropertyAccessor = PropertyAccessorFactory.forBeanPropertyAccess(entity);
        for (String key : entityProperties.keys()) {
            JavaType entityValueType = getType(entityPropertyAccessor.getPropertyTypeDescriptor(key));
            Collection<Object> entityValueAsCollection = entityProperties.get(key);
            if (entityValueType.isTypeOrSubTypeOf(Collection.class)) {
                Object entityValue = entityValueAsCollection;
                entityValue = objectMapper.convertValue(entityValue, entityValueType);
                entityPropertyAccessor.setPropertyValue(key, entityValue);
            } else {
                if (entityValueAsCollection.size() == 1) {
                    entityPropertyAccessor.setPropertyValue(key, entityValueAsCollection.iterator().next());
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
