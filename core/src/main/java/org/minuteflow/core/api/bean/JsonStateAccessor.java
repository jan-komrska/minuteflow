package org.minuteflow.core.api.bean;

import java.util.Collections;
import java.util.HashMap;

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
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.collections4.SetUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.minuteflow.core.api.contract.CalculatedState;
import org.minuteflow.core.api.contract.State;
import org.minuteflow.core.api.exception.EntityUpdateRejectedException;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Getter;
import lombok.Setter;

public class JsonStateAccessor<Entity> extends BaseStateAccessor<Entity> {
    @Autowired
    private ObjectMapper objectMapper;

    private Set<State> managedStates = null;
    private Set<State> calculatedStates = new HashSet<State>();
    private Map<String, State> persistentStates = new HashMap<String, State>();

    @Getter
    @Setter
    private Function<Entity, String> stateGetter = null;

    @Getter
    @Setter
    private BiConsumer<Entity, String> stateSetter = null;

    //

    public JsonStateAccessor(Class<Entity> entityClass) {
        super(entityClass);
    }

    //

    @Override
    protected Set<State> getStatesImpl(Entity entity) {
        Objects.requireNonNull(entity);
        //
        String statesNamesAsString = stateGetter.apply(entity);
        if (StringUtils.isEmpty(statesNamesAsString)) {
            return Set.of();
        }
        //
        String[] stateNames;
        try {
            stateNames = objectMapper.readValue(statesNamesAsString, String[].class);
            stateNames = ArrayUtils.nullToEmpty(stateNames);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException();
        }
        //
        Set<State> states = new HashSet<State>();
        for (String stateName : stateNames) {
            State state = persistentStates.get(stateName);
            if (state != null) {
                states.add(state);
            } else {
                throw new IllegalStateException();
            }
        }
        //
        states.addAll(calculatedStates);
        //
        return states;
    }

    @Override
    protected void setStatesImpl(Entity entity, Set<State> states) throws EntityUpdateRejectedException {
        Objects.requireNonNull(entity);
        //
        Set<String> stateNames = SetUtils.emptyIfNull(states).stream(). //
                map(State::getName).collect(Collectors.toSet());
        //
        String stateNamesAsString;
        try {
            stateNamesAsString = objectMapper.writeValueAsString(stateNames);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException();
        }
        //
        stateSetter.accept(entity, stateNamesAsString);
    }

    //

    public Set<State> getManagedStates() {
        return managedStates;
    }

    public void setManagedStates(Set<State> managedStates) {
        this.managedStates = managedStates;
        //
        if (managedStates != null) {
            persistentStates = SetUtils.emptyIfNull(managedStates).stream(). //
                    filter((state) -> !(state instanceof CalculatedState)). //
                    collect(Collectors.toUnmodifiableMap(State::getName, Function.identity()));
            calculatedStates = SetUtils.emptyIfNull(managedStates).stream(). //
                    filter((state) -> (state instanceof CalculatedState)). //
                    collect(Collectors.toUnmodifiableSet());
        } else {
            persistentStates = Collections.emptyMap();
            calculatedStates = Collections.emptySet();
        }
    }

    public JsonStateAccessor<Entity> withManagedStates(Set<State> managedStates) {
        setManagedStates(managedStates);
        return this;
    }

    public JsonStateAccessor<Entity> withManagedStates(State... managedStates) {
        setManagedStates((managedStates != null) ? Set.of(managedStates) : null);
        return this;
    }

    public JsonStateAccessor<Entity> withAccessors(Function<Entity, String> stateGetter, BiConsumer<Entity, String> stateSetter) {
        this.stateGetter = stateGetter;
        this.stateSetter = stateSetter;
        return this;
    }
}
