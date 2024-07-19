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

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.PredicateUtils;
import org.apache.commons.collections4.SetUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.minuteflow.core.api.contract.Source;
import org.minuteflow.core.api.contract.State;
import org.minuteflow.core.api.contract.StateAccessor;
import org.minuteflow.core.api.contract.StateManager;
import org.minuteflow.core.api.exception.BaseException;
import org.minuteflow.core.api.exception.EntityNotSupportedException;
import org.minuteflow.core.api.exception.EntityUpdateRejectedException;
import org.minuteflow.core.impl.repository.StateAccessorRepository;
import org.minuteflow.core.impl.repository.StateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class BaseStateManager implements StateManager {
    @Autowired
    private StateRepository stateRepository;

    @Autowired
    private StateAccessorRepository stateAccessorRepository;

    @Value("${minuteflow.accessor.active-group-names:default}")
    private List<String> activeGroupNames;

    //

    public BaseStateManager() {
    }

    //

    @Override
    public State valueOf(String stateName) {
        return stateRepository.getState(stateName);
    }

    @Override
    public Set<State> getAllStates(String... patterns) {
        patterns = ArrayUtils.nullToEmpty(patterns);
        //
        HashSet<State> states = new HashSet<State>();
        for (String pattern : patterns) {
            states.addAll(stateRepository.getAllStates(pattern));
        }
        return states;
    }

    private StateAccessor findStateAccessor(Object entity) throws EntityNotSupportedException {
        for (String activeGroupName : activeGroupNames) {
            Class<?> entityClass = (entity != null) ? entity.getClass() : null;
            while (entityClass != null) {
                StateAccessor stateAccessor = stateAccessorRepository.getStateAccessor(activeGroupName, entityClass);
                if ((stateAccessor != null) && stateAccessor.isSupported(entity)) {
                    return stateAccessor;
                }
                //
                entityClass = entityClass.getSuperclass();
            }
        }
        //
        throw new EntityNotSupportedException();
    }

    private Set<State> getStates(Object entity, StateAccessor stateAccessor) {
        Set<State> sourceStates = SetUtils.emptyIfNull(stateAccessor.getStates(entity));
        //
        Set<State> states = new HashSet<State>();
        CollectionUtils.addAll(states, sourceStates);
        CollectionUtils.filter(states, PredicateUtils.notNullPredicate());
        return states;
    }

    private void setStates(Object entity, StateAccessor stateAccessor, Set<State> sourceStates) throws EntityUpdateRejectedException {
        sourceStates = SetUtils.emptyIfNull(sourceStates);
        //
        Set<State> states = new HashSet<State>();
        CollectionUtils.addAll(states, sourceStates);
        CollectionUtils.filter(states, PredicateUtils.notNullPredicate());
        stateAccessor.setStates(entity, states);
    }

    private Set<State> asSet(State... sourceStates) {
        sourceStates = ArrayUtils.nullToEmpty(sourceStates, State[].class);
        //
        Set<State> states = new HashSet<State>();
        CollectionUtils.addAll(states, sourceStates);
        CollectionUtils.filter(states, PredicateUtils.notNullPredicate());
        return states;
    }

    //

    @SuppressWarnings("unchecked")
    private <Entity> Source<Entity> asSource(Object entity) {
        return (entity instanceof Source<?>) ? (Source<Entity>) entity : null;
    }

    private <Entity> Entity getEntity(Source<Entity> source, Entity entity) {
        if ((source != null) && source.isResolved()) {
            return source.getEntity();
        } else {
            return entity;
        }
    }

    private void markForUpdate(Source<?> source) {
        if ((source != null) && source.isResolved()) {
            source.markForUpdate();
        }
    }

    //

    @Override
    public Set<State> getStates(Object entity) throws BaseException {
        StateAccessor stateAccessor = findStateAccessor(entity);
        return getStates(entity, stateAccessor);
    }

    @Override
    public void setStates(Object entity, Set<State> states) throws BaseException {
        Source<Object> source = asSource(entity);
        entity = getEntity(source, entity);
        //
        StateAccessor stateAccessor = findStateAccessor(entity);
        setStates(entity, stateAccessor, states);
        //
        markForUpdate(source);
    }

    @Override
    public void setStates(Object entity, State... states) throws BaseException {
        states = ArrayUtils.nullToEmpty(states, State[].class);
        setStates(entity, Set.of(states));
    }

    @Override
    public boolean containsState(Object entity, State... sourceStates) throws BaseException {
        StateAccessor stateAccessor = findStateAccessor(entity);
        Set<State> states = getStates(entity, stateAccessor);
        return states.containsAll(asSet(sourceStates));
    }

    @Override
    public void addState(Object entity, State... sourceStates) throws BaseException {
        Source<Object> source = asSource(entity);
        entity = getEntity(source, entity);
        //
        StateAccessor stateAccessor = findStateAccessor(entity);
        Set<State> states = getStates(entity, stateAccessor);
        states.addAll(asSet(sourceStates));
        setStates(entity, stateAccessor, states);
        //
        markForUpdate(source);
    }

    @Override
    public void removeState(Object entity, State... sourceStates) throws BaseException {
        Source<Object> source = asSource(entity);
        entity = getEntity(source, entity);
        //
        StateAccessor stateAccessor = findStateAccessor(entity);
        Set<State> states = getStates(entity, stateAccessor);
        states.removeAll(asSet(sourceStates));
        setStates(entity, stateAccessor, states);
        //
        markForUpdate(source);
    }

    @Override
    public void updateState(Object entity, State sourceState, State targetState) throws BaseException {
        Objects.requireNonNull(sourceState);
        Objects.requireNonNull(targetState);
        //
        Source<Object> source = asSource(entity);
        entity = getEntity(source, entity);
        //
        StateAccessor stateAccessor = findStateAccessor(entity);
        Set<State> states = getStates(entity, stateAccessor);
        if (!states.contains(sourceState)) {
            throw new EntityUpdateRejectedException();
        }
        states.remove(sourceState);
        states.add(targetState);
        setStates(entity, stateAccessor, states);
        //
        markForUpdate(source);
    }

    @Override
    public void updateState(Object entity, State[] sourceStatesAsArray, State[] targetStatesAsArray) throws BaseException {
        Set<State> sourceStates = asSet(sourceStatesAsArray);
        Set<State> targetStates = asSet(targetStatesAsArray);
        //
        Source<Object> source = asSource(entity);
        entity = getEntity(source, entity);
        //
        StateAccessor stateAccessor = findStateAccessor(entity);
        Set<State> states = getStates(entity, stateAccessor);
        if (!states.containsAll(sourceStates)) {
            throw new EntityUpdateRejectedException();
        }
        states.removeAll(sourceStates);
        states.addAll(targetStates);
        setStates(entity, stateAccessor, states);
        //
        markForUpdate(source);
    }
}
