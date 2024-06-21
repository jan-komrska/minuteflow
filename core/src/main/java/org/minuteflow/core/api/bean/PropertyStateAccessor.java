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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.collections4.SetUtils;
import org.minuteflow.core.api.contract.CalculatedState;
import org.minuteflow.core.api.contract.PropertyState;
import org.minuteflow.core.api.contract.State;
import org.minuteflow.core.api.exception.EntityUpdateRejectedException;
import org.springframework.beans.PropertyAccessor;
import org.springframework.beans.PropertyAccessorFactory;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PropertyStateAccessor<Entity extends Object> extends BaseStateAccessor<Entity> {
    private Set<State> managedStates = null;

    //

    public PropertyStateAccessor(Class<Entity> entityClass) {
        super(entityClass);
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
                for (Map.Entry<String, Object> stateEntry : propertyState.getProperties().entrySet()) {
                    Object entityValue = entityPropertyAccessor.getPropertyValue(stateEntry.getKey());
                    applied = applied && Objects.equals(entityValue, stateEntry.getValue());
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
        PropertyAccessor entityPropertyAccessor = PropertyAccessorFactory.forBeanPropertyAccess(entity);
        Map<String, Object> entityProperties = new HashMap<String, Object>();
        //
        for (State state : SetUtils.emptyIfNull(states)) {
            if (state instanceof PropertyState propertyState) {
                for (Map.Entry<String, Object> stateEntry : propertyState.getProperties().entrySet()) {
                    Object previousValue = entityProperties.putIfAbsent(stateEntry.getKey(), stateEntry.getValue());
                    if (previousValue != null) {
                        throw new EntityUpdateRejectedException();
                    }
                }
            }
        }
        //
        for (Map.Entry<String, Object> entityEntry : entityProperties.entrySet()) {
            entityPropertyAccessor.setPropertyValue(entityEntry.getKey(), entityEntry.getValue());
        }
    }

    public PropertyStateAccessor<Entity> withManagedStates(State... managedStates) {
        setManagedStates((managedStates != null) ? Set.of(managedStates) : null);
        return this;
    }
}
