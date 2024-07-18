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

import java.util.List;
import java.util.Objects;

import org.apache.commons.collections4.ListUtils;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString(onlyExplicitlyIncluded = true)
class SourceWithEntity<Entity> implements Source<Entity> {
    private final String name;
    private final List<Object> parameters;

    @ToString.Include
    private final Entity entity;

    private final boolean resolved = true;
    private boolean forUpdate = false;
    private boolean forDelete = false;

    //

    public SourceWithEntity(String name, List<Object> parameters, Entity entity) {
        this.name = name;
        this.parameters = ListUtils.emptyIfNull(parameters).stream().toList();
        this.entity = Objects.requireNonNull(entity);
    }

    //

    @Override
    public void markForUpdate() {
        forUpdate = true;
    }

    @Override
    public void markForDelete() {
        forDelete = true;
    }
}
