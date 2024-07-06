package org.minuteflow.core.api.contract;

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

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import lombok.AccessLevel;
import lombok.Getter;

@Getter
class SourceWithEntity<Entity> implements Source<Entity> {
    private List<Object> parameters = Collections.emptyList();

    @Getter(AccessLevel.NONE)
    private Entity entity = null;

    private boolean loaded = false;
    private boolean saved = false;
    private boolean deleted = false;

    //

    public SourceWithEntity(Entity entity) {
        this.entity = Objects.requireNonNull(entity);
        this.loaded = true;
    }

    //

    @Override
    public Entity getEntity() {
        if (loaded) {
            return entity;
        } else {
            throw new IllegalStateException();
        }
    }

    @Override
    public Entity saveEntity() {
        if (loaded) {
            saved = true;
            return entity;
        } else {
            throw new IllegalStateException();
        }
    }

    @Override
    public void deleteEntity() {
        if (loaded) {
            loaded = false;
            deleted = true;
        } else {
            throw new IllegalStateException();
        }
    }
}
