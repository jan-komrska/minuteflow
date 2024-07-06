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

import java.util.Arrays;
import java.util.List;

public interface Source<Entity> {
    public List<Object> getParameters();

    //

    public boolean isResolved();

    public boolean isSaved();

    public boolean isDeleted();

    //

    public Entity getEntity();

    public Entity saveEntity();

    public void deleteEntity();

    //

    public static <Entity> Source<Entity> withParameters(Object... parameters) {
        List<Object> parametersAsList = (parameters != null) ? Arrays.asList(parameters) : null;
        return new SourceWithParameters<Entity>(parametersAsList);
    }

    public static <Entity> Source<Entity> withEntity(Entity entity) {
        return new SourceWithEntity<Entity>(entity);
    }
}
