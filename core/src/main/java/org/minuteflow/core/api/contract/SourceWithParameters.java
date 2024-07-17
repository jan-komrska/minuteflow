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

import org.apache.commons.collections4.ListUtils;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString(onlyExplicitlyIncluded = true)
class SourceWithParameters<Entity> implements Source<Entity> {
    private final String name;

    @ToString.Include
    private final List<Object> parameters;

    //

    public SourceWithParameters(String name, List<Object> parameters) {
        this.name = name;
        this.parameters = ListUtils.emptyIfNull(parameters).stream().toList();
    }

    public SourceWithParameters(List<Object> parameters) {
        this(null, parameters);
    }

    //

    @Override
    public boolean isResolved() {
        return false;
    }

    @Override
    public boolean isForUpdate() {
        return false;
    }

    @Override
    public boolean isForDelete() {
        return false;
    }

    @Override
    public Entity getEntity() {
        throw new IllegalStateException();
    }

    @Override
    public void markForUpdate() {
        throw new IllegalStateException();
    }

    @Override
    public void markForDelete() {
        throw new IllegalStateException();
    }
}
