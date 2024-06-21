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

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.SetUtils;
import org.minuteflow.core.api.contract.CalculatedState;
import org.minuteflow.core.api.contract.State;
import org.minuteflow.core.api.contract.StateAccessor;
import org.minuteflow.core.api.exception.EntityUpdateRejectedException;

import lombok.ToString;

@ToString
public abstract class BaseStateAccessor<Entity> implements StateAccessor {
    private String groupName = null;
    private Class<Entity> entityClass = null;

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
            Set<State> states = new HashSet<State>(SetUtils.emptyIfNull( //
                    getStatesImpl(entityClass.cast(entity))));
            filterCalculatedStates(states);
            return states;
        } else {
            throw new IllegalArgumentException();
        }
    }

    public void setStates(Object entity, Set<State> states) throws EntityUpdateRejectedException {
        if (isSupported(entity)) {
            states = new HashSet<State>(SetUtils.emptyIfNull(states));
            removeCalculatedStates(states);
            setStatesImpl(entityClass.cast(entity), states);
        } else {
            throw new IllegalArgumentException();
        }
    }

    //

    protected abstract Set<State> getStatesImpl(Entity entity);

    protected abstract void setStatesImpl(Entity entity, Set<State> states) throws EntityUpdateRejectedException;

    protected void filterCalculatedStates(Set<State> states) {
        Set<State> persistentStates = new HashSet<>(SetUtils.emptyIfNull(states));
        CollectionUtils.filter(persistentStates, (state) -> !(state instanceof CalculatedState));
        //
        CollectionUtils.filter(states, (state) -> {
            if (state instanceof CalculatedState calculatedState) {
                return calculatedState.appliesTo(persistentStates);
            } else {
                return true;
            }
        });
    }

    protected void removeCalculatedStates(Set<State> states) {
        CollectionUtils.filter(states, (state) -> !(state instanceof CalculatedState));
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
}
