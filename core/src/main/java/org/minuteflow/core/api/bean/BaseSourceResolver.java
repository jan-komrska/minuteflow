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
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.minuteflow.core.api.contract.Source;
import org.minuteflow.core.api.contract.SourceResolver;
import org.minuteflow.core.api.exception.SourceNotSupportedException;
import org.springframework.data.repository.CrudRepository;

import lombok.Getter;

@Getter
public class BaseSourceResolver<Entity> implements SourceResolver<Entity> {
    @Getter
    private class EmbeddedSource implements Source<Entity> {
        private final List<Object> parameters;
        private Entity entity = null;

        private final boolean resolved = true;
        private boolean saved = false;
        private boolean deleted = false;

        //

        public EmbeddedSource(List<Object> parameters, Entity entity) {
            this.parameters = ListUtils.emptyIfNull(parameters).stream().toList();
            this.entity = Objects.requireNonNull(entity);
        }

        //

        @Override
        public Entity saveEntity() {
            entity = BaseSourceResolver.this.saveEntity(entity);
            return entity;
        }

        @Override
        public void deleteEntity() {
            BaseSourceResolver.this.deleteEntity(entity);
            deleted = true;
        }
    }

    //

    private Class<Entity> entityClass = null;
    private CrudRepository<Entity, ?> crudRepository = null;

    //

    public BaseSourceResolver(Class<Entity> entityClass, CrudRepository<Entity, ?> crudRepository) {
        this.entityClass = entityClass;
        this.crudRepository = crudRepository;
    }

    //

    @Override
    public Source<Entity> resolve(Source<?> source) throws SourceNotSupportedException {
        if (source.isResolved()) {
            throw new IllegalStateException();
        }
        //
        Entity entity = loadEntity(source.getParameters());
        return new EmbeddedSource(source.getParameters(), entity);
    }

    protected Entity loadEntity(List<Object> parameters) {
        if (CollectionUtils.isEmpty(parameters)) {
            throw new IllegalStateException();
        }
        //
        String methodName;
        if (parameters.get(0) instanceof String value) {
            methodName = value;
        } else {
            throw new IllegalStateException();
        }
        //
        Object[] args = parameters.subList(1, parameters.size()).toArray();
        //
        Object result;
        try {
            result = MethodUtils.invokeMethod(crudRepository, methodName, args);
            result = (result instanceof Optional<?> optional) ? optional.orElse(null) : result;
        } catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException ex) {
            throw new IllegalStateException(ex);
        }
        //
        if (entityClass.isInstance(result)) {
            return entityClass.cast(result);
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
