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

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.collections4.SetUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.minuteflow.core.api.contract.CalculatedState;
import org.minuteflow.core.api.contract.State;
import org.minuteflow.core.api.contract.StateAccessor;
import org.minuteflow.core.api.exception.EntityUpdateRejectedException;

import lombok.ToString;

@ToString
public abstract class BaseStateAccessor<Entity> implements StateAccessor {
    private String groupName = null;
    private Class<Entity> entityClass = null;
    private Set<CalculatedState> calculatedStates = new HashSet<CalculatedState>();

    //

    public BaseStateAccessor(Class<Entity> entityClass) {
        this.entityClass = entityClass;
    }

    //

    public boolean isSupported(Object entity) {
        return entityClass.isInstance(entity);
    }

    public Set<State> getStates(Object entity) {
        if (isSupported(entity)) {
            Set<State> states = SetUtils.emptyIfNull(getStatesImpl(entityClass.cast(entity)));
            Set<CalculatedState> calculatedStates = SetUtils.emptyIfNull(applyCalculatedStates(states));
            //
            HashSet<State> allStates = new HashSet<State>();
            allStates.addAll(states);
            allStates.addAll(calculatedStates);
            return allStates;
        } else {
            throw new IllegalArgumentException();
        }
    }

    public void setStates(Object entity, Set<State> states) throws EntityUpdateRejectedException {
        if (isSupported(entity)) {
            states = removeCalculatedStates(states);
            setStatesImpl(entityClass.cast(entity), states);
        } else {
            throw new IllegalArgumentException();
        }
    }

    //

    protected abstract Set<State> getStatesImpl(Entity entity);

    protected abstract void setStatesImpl(Entity entity, Set<State> states) throws EntityUpdateRejectedException;

    protected Set<CalculatedState> applyCalculatedStates(Set<State> persistentStates) {
        Set<CalculatedState> appliedStates = new HashSet<CalculatedState>();
        for (CalculatedState calculatedState : calculatedStates) {
            if (calculatedState.appliesTo(persistentStates)) {
                appliedStates.add(calculatedState);
            }
        }
        return appliedStates;
    }

    protected Set<State> removeCalculatedStates(Set<State> allStates) {
        HashSet<State> states = new HashSet<State>(SetUtils.emptyIfNull(allStates));
        states.removeIf((state) -> (state instanceof CalculatedState));
        return states;
    }

    //

    @Override
    public String getGroupName() {
        return Objects.toString(this.groupName, StateAccessor.DEFAULT_GROUP_NAME);
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    @Override
    public Class<?> getEntityClass() {
        return entityClass;
    }

    public Set<CalculatedState> getCalculatedStates() {
        return SetUtils.unmodifiableSet(calculatedStates);
    }

    public void setCalculatedStates(Set<CalculatedState> calculatedStates) {
        this.calculatedStates = new HashSet<CalculatedState>(SetUtils.emptyIfNull(calculatedStates));
    }

    public void setCalculatedStates(CalculatedState... calculatedStates) {
        calculatedStates = ArrayUtils.nullToEmpty(calculatedStates, CalculatedState[].class);
        setCalculatedStates(Set.of(calculatedStates));
    }
}
