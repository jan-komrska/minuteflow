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

import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.minuteflow.core.api.contract.Source;
import org.minuteflow.core.api.contract.SourceResolver;
import org.minuteflow.core.api.exception.EntityNotFoundException;
import org.minuteflow.core.api.exception.SourceNotSupportedException;
import org.springframework.data.repository.CrudRepository;

import lombok.Getter;

@Getter
public class BaseSourceResolver<Entity> implements SourceResolver<Entity> {
    private Class<Entity> entityClass = null;
    private CrudRepository<Entity, ?> crudRepository = null;
    private String defaultMethodName = null;

    //

    public BaseSourceResolver(Class<Entity> entityClass, CrudRepository<Entity, ?> crudRepository, String defaultMethodName) {
        this.entityClass = entityClass;
        this.crudRepository = crudRepository;
        this.defaultMethodName = StringUtils.defaultIfEmpty(defaultMethodName, null);
    }

    //

    @Override
    public Source<Entity> resolve(String name, List<Object> parameters) throws SourceNotSupportedException {
        name = StringUtils.defaultIfEmpty(name, defaultMethodName);
        name = Objects.requireNonNull(name);
        //
        Object result;
        try {
            Object[] args = ListUtils.emptyIfNull(parameters).toArray();
            result = MethodUtils.invokeMethod(crudRepository, name, args);
            result = (result instanceof Optional<?> optional) ? optional.orElse(null) : result;
        } catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException ex) {
            throw new IllegalStateException(ex);
        }
        //
        if (entityClass.isInstance(result)) {
            Entity entity = entityClass.cast(result);
            return Source.with(name, parameters, entity);
        } else {
            throw new EntityNotFoundException();
        }
    }

    @Override
    public void commit(Source<Entity> source) {
        boolean isResolvedInstance = (source != null) && source.isResolved() && //
                entityClass.isInstance(source.getEntity());
        //
        if (isResolvedInstance) {
            if (source.isForDelete()) {
                crudRepository.delete(source.getEntity());
            } else if (source.isForUpdate()) {
                crudRepository.save(source.getEntity());
            } else {
                // DO NOTHING
            }
        } else {
            throw new IllegalStateException();
        }
    }
}
