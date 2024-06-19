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
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.collections4.SetUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.minuteflow.core.api.contract.CalculatedState;
import org.minuteflow.core.api.contract.State;
import org.minuteflow.core.api.contract.StateCollection;
import org.minuteflow.core.api.exception.EntityUpdateRejectedException;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JsonStateAccessor<Entity> extends BaseStateAccessor<Entity> {
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private StateCollection stateCollection;

    private Function<Entity, String> stateGetter = null;
    private BiConsumer<Entity, String> stateSetter = null;

    @Getter
    @Setter(AccessLevel.NONE)
    private Set<CalculatedState> calculatedStates = null;

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
            State state = stateCollection.valueOf(stateName);
            if (state != null) {
                states.add(state);
            } else {
                throw new IllegalStateException();
            }
        }
        //
        states.addAll(SetUtils.emptyIfNull(calculatedStates));
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

    public void setCalculatedStates(Set<CalculatedState> calculatedStates) {
        this.calculatedStates = calculatedStates;
    }

    public void setCalculatedStates(CalculatedState... calculatedStates) {
        setCalculatedStates((calculatedStates != null) ? Set.of(calculatedStates) : null);
    }
}
