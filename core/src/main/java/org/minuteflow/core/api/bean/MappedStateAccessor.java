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

import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.SetUtils;
import org.minuteflow.core.api.contract.State;
import org.minuteflow.core.api.exception.EntityUpdateRejectedException;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MappedStateAccessor<Entity, EntityState> extends BaseStateAccessor<Entity> {
    @Setter(AccessLevel.NONE)
    private Class<EntityState> entityStateClass = null;

    private BidiMap<EntityState, State> stateMap = null;
    private Function<Entity, EntityState> stateGetter = null;
    private BiConsumer<Entity, EntityState> stateSetter = null;

    //

    public MappedStateAccessor(Class<Entity> entityClass, Class<EntityState> entityStateClass) {
        super(entityClass);
        this.entityStateClass = entityStateClass;
    }

    //

    @Override
    protected Set<State> getStatesImpl(Entity entity) {
        Objects.requireNonNull(entity);
        //
        EntityState entityState = stateGetter.apply(entity);
        State state = (entityState != null) ? stateMap.get(entityState) : null;
        return (state != null) ? Set.of(state) : Set.of();
    }

    @Override
    protected void setStatesImpl(Entity entity, Set<State> states) throws EntityUpdateRejectedException {
        Objects.requireNonNull(entity);
        states = SetUtils.emptyIfNull(states);
        //
        if (states.size() == 0) {
            stateSetter.accept(entity, null);
        } else if (states.size() == 1) {
            EntityState entityState = stateMap.getKey(states.iterator().next());
            stateSetter.accept(entity, entityState);
        } else {
            throw new EntityUpdateRejectedException();
        }
    }
}
