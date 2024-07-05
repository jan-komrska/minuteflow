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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;

import lombok.Getter;

@Getter
class ImmutableSource<Entity> implements Source<Entity> {
    private String name = null;
    private List<Object> parameters = null;

    //

    public ImmutableSource(String name, List<Object> parameters) {
        parameters = ListUtils.emptyIfNull(parameters);
        //
        this.name = StringUtils.defaultString(name, DEFAULT_NAME);
        this.parameters = Collections.unmodifiableList(new ArrayList<Object>(parameters));
    }

    public ImmutableSource(List<Object> parameters) {
        this(null, parameters);
    }

    //

    @Override
    public boolean isActive() {
        return false;
    }

    @Override
    public Entity getEntity() {
        throw new IllegalStateException();
    }

    @Override
    public void saveEntity() {
        throw new IllegalStateException();
    }

    @Override
    public boolean deleteEntity() {
        throw new IllegalStateException();
    }
}
