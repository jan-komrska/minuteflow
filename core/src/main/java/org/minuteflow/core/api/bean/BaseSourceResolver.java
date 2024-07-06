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

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.List;
import java.util.Objects;

import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.reflect.TypeUtils;
import org.minuteflow.core.api.contract.Source;
import org.minuteflow.core.api.contract.SourceResolver;
import org.minuteflow.core.api.exception.SourceNotSupportedException;
import org.springframework.data.repository.CrudRepository;

import lombok.Getter;

@Getter
public abstract class BaseSourceResolver<Entity> implements SourceResolver {
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
        public boolean isResolved() {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean isSaved() {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean isDeleted() {
            // TODO Auto-generated method stub
            return false;
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

    private Class<?> contractClass = null;
    private Class<?> entityClass = null;
    private CrudRepository<Entity, ?> crudRepository = null;

    //

    public BaseSourceResolver(Class<?> contractClass, Class<Entity> entityClass, CrudRepository<Entity, ?> crudRepository) {
        this.contractClass = contractClass;
        this.entityClass = entityClass;
        this.crudRepository = crudRepository;
    }

    //

    private Type getType(Type type, TypeVariable<? extends Class<?>> variable) {
        return TypeUtils.getTypeArguments(type, variable.getGenericDeclaration()).get(variable);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <OtherEntity> Source<OtherEntity> resolve(Source<OtherEntity> source, Type sourceType) //
            throws SourceNotSupportedException {
        if (source.isResolved() && !source.isDeleted()) {
            throw new IllegalStateException();
        }
        //
        Type entityType = getType(sourceType, Source.class.getTypeParameters()[0]);
        Entity entity = loadEntity(source.getParameters());
        //
        if (TypeUtils.isInstance(entity, entityType)) {
            return (Source<OtherEntity>) new EmbeddedSource(source.getParameters(), entity);
        } else {
            throw new IllegalStateException();
        }
    }

    //

    protected abstract Entity loadEntity(List<Object> parameters);

    protected abstract Entity saveEntity(Entity entity);

    protected abstract void deleteEntity(Entity entity);
}
