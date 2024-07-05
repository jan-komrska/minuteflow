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

import java.util.List;
import java.util.Objects;

import org.apache.commons.collections4.ListUtils;
import org.minuteflow.core.api.contract.Source;
import org.minuteflow.core.api.contract.SourceResolver;
import org.minuteflow.core.api.exception.SourceNotSupportedException;

import lombok.Getter;

@Getter
public abstract class BaseSourceResolver<Entity> implements SourceResolver<Entity> {
    @Getter
    private class EmbeddedSource implements Source<Entity> {
        private List<Object> parameters = null;
        private Entity entity = null;
        private boolean active = false;

        public EmbeddedSource(List<Object> parameters, Entity entity) {
            this.parameters = ListUtils.emptyIfNull(parameters).stream().toList();
            this.entity = Objects.requireNonNull(entity);
            this.active = true;
        }

        @Override
        public Entity getEntity() {
            if (active) {
                return entity;
            } else {
                throw new IllegalStateException();
            }
        }

        @Override
        public Entity saveEntity() {
            if (active) {
                this.entity = BaseSourceResolver.this.saveEntity(entity);
                return this.entity;
            } else {
                throw new IllegalStateException();
            }
        }

        @Override
        public void deleteEntity() {
            if (active) {
                BaseSourceResolver.this.deleteEntity(entity);
                this.active = false;
            } else {
                throw new IllegalStateException();
            }
        }

    }

    //

    private Class<Entity> entityClass = null;

    public BaseSourceResolver(Class<Entity> entityClass) {
        this.entityClass = entityClass;
    }

    @Override
    public Source<Entity> resolve(Source<Entity> source) throws SourceNotSupportedException {
        if (!source.isActive()) {
            Entity entity = loadEntity(source.getParameters());
            return new EmbeddedSource(source.getParameters(), entity);
        } else {
            return source;
        }
    }

    //

    protected abstract Entity loadEntity(List<Object> parameters);

    protected abstract Entity saveEntity(Entity entity);

    protected abstract void deleteEntity(Entity entity);
}
