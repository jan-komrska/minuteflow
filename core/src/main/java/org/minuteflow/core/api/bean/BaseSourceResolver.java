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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.List;
import java.util.Objects;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.apache.commons.lang3.reflect.TypeUtils;
import org.minuteflow.core.api.contract.Source;
import org.minuteflow.core.api.contract.SourceResolver;
import org.minuteflow.core.api.exception.SourceNotSupportedException;
import org.springframework.data.repository.CrudRepository;

import lombok.Getter;

@Getter
public class BaseSourceResolver<Entity> implements SourceResolver {
    @Getter
    private class EmbeddedSource implements Source<Entity> {
        private List<Object> parameters = null;
        private Entity entity = null;

        private boolean resolved = false;
        private boolean saved = false;
        private boolean deleted = false;

        //

        public EmbeddedSource(List<Object> parameters, Entity entity) {
            this.parameters = ListUtils.emptyIfNull(parameters).stream().toList();
            this.entity = Objects.requireNonNull(entity);
            this.resolved = true;
        }

        //

        @Override
        public Entity getEntity() {
            if (resolved) {
                return entity;
            } else {
                throw new IllegalStateException();
            }
        }

        @Override
        public Entity saveEntity() {
            if (resolved) {
                entity = BaseSourceResolver.this.saveEntity(entity);
                return entity;
            } else {
                throw new IllegalStateException();
            }
        }

        @Override
        public void deleteEntity() {
            if (resolved) {
                BaseSourceResolver.this.deleteEntity(entity);
                deleted = true;
            } else {
                throw new IllegalStateException();
            }
        }
    }

    //

    private Class<?> contractClass = null;
    private Class<Entity> entityClass = null;
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

    protected Entity loadEntity(List<Object> parameters) {
        if (CollectionUtils.isEmpty(parameters)) {
            throw new IllegalStateException();
        }
        //
        if (parameters.get(0) instanceof String methodName) {
            Object[] args = parameters.subList(1, parameters.size()).toArray();
            //
            try {
                return entityClass.cast(MethodUtils.invokeMethod(crudRepository, methodName, args));
            } catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException ex) {
                throw new IllegalStateException(ex);
            }
        } else {
            throw new IllegalStateException();
        }
    }

    protected Entity saveEntity(Entity entity) {
        return crudRepository.save(entity);
    }

    protected void deleteEntity(Entity entity) {
        crudRepository.delete(entity);
    }
}
