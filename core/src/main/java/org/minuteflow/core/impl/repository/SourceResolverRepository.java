package org.minuteflow.core.impl.repository;

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

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.minuteflow.core.api.contract.SourceResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class SourceResolverRepository {
    @Getter
    @Setter
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    private static class SourceResolverId {
        private Class<?> contractClass = null;
    }

    //

    @Autowired
    private ApplicationContext applicationContext = null;

    private Map<SourceResolverId, String> sourceResolverMap = new ConcurrentHashMap<SourceResolverId, String>();

    //

    public SourceResolverRepository() {
    }

    //

    public SourceResolver getSourceResolver(Class<?> contractClass) {
        SourceResolverId stateAccessorId = new SourceResolverId(contractClass);
        if (sourceResolverMap.containsKey(stateAccessorId)) {
            String beanName = sourceResolverMap.get(stateAccessorId);
            return applicationContext.getBean(beanName, SourceResolver.class);
        } else {
            return null;
        }
    }

    public void addSourceResolver(SourceResolver sourceResolver, String beanName) {
        Objects.requireNonNull(sourceResolver);
        Objects.requireNonNull(beanName);
        //
        SourceResolverId sourceResolverId = new SourceResolverId(sourceResolver.getContractClass());
        String registeredBeanName = sourceResolverMap.putIfAbsent(sourceResolverId, beanName);
        if (Objects.isNull(registeredBeanName)) {
            log.debug("registered source resolver [" + sourceResolverId + "] implemented by [" + beanName + "]");
        } else {
            throw new IllegalStateException();
        }
    }

    public void removeSourceResolver(SourceResolver sourceResolver, String beanName) {
        Objects.requireNonNull(sourceResolver);
        Objects.requireNonNull(beanName);
        //
        SourceResolverId sourceResolverId = new SourceResolverId(sourceResolver.getContractClass());
        boolean removed = sourceResolverMap.remove(sourceResolverId, beanName);
        if (removed) {
            log.debug("unregistered source resolver [" + sourceResolverId + "]");
        }
    }
}
