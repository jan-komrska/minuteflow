package org.minuteflow.core.impl.factory;

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
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.minuteflow.core.api.annotation.ControllerRef;
import org.minuteflow.core.api.annotation.ControllerRefs;
import org.minuteflow.core.api.bean.BaseController;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.annotation.Scope;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class MinuteFlowPostProcessor implements BeanDefinitionRegistryPostProcessor, Ordered {
    private AtomicLong beanNameSequence = new AtomicLong(0);

    public String nextBeanName(String type) {
        return "minute-flow:" + type + ":" + beanNameSequence.addAndGet(1);
    }

    //

    private List<MergedAnnotation<ControllerRef>> getControllerRefs(MergedAnnotations mergedAnnotations) {
        ArrayList<MergedAnnotation<ControllerRef>> controllerRefs = new ArrayList<MergedAnnotation<ControllerRef>>();
        //
        mergedAnnotations.stream(ControllerRefs.class).flatMap((annotation) -> {
            return Arrays.stream(annotation.getAnnotationArray("value", ControllerRef.class));
        }).forEach(controllerRefs::add);
        //
        mergedAnnotations.stream(ControllerRef.class).forEach(controllerRefs::add);
        //
        return controllerRefs;
    }

    private void registerController(BeanDefinitionRegistry registry, String stateName, String serviceName) {
        String controllerName = nextBeanName("controller");
        //
        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(BaseController.class);
        beanDefinitionBuilder.setScope(BeanDefinition.SCOPE_SINGLETON);
        beanDefinitionBuilder.setLazyInit(false);
        beanDefinitionBuilder.addPropertyReference("parentState", stateName);
        beanDefinitionBuilder.addPropertyReference("service", serviceName);
        //
        registry.registerBeanDefinition(controllerName, beanDefinitionBuilder.getBeanDefinition());
        //
        log.debug("Registered controller [" + stateName, "," + serviceName + "]");
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        String[] beanNames = ArrayUtils.nullToEmpty(registry.getBeanDefinitionNames());
        for (String beanName : beanNames) {
            BeanDefinition abstractBeanDefinition = registry.getBeanDefinition(beanName);
            if (abstractBeanDefinition instanceof AnnotatedBeanDefinition beanDefinition) {
                AnnotatedTypeMetadata metadata;
                if (StringUtils.isNotEmpty(beanDefinition.getFactoryBeanName())) {
                    metadata = beanDefinition.getFactoryMethodMetadata();
                } else {
                    metadata = beanDefinition.getMetadata();
                }
                //
                if (metadata != null) {
                    MergedAnnotations mergedAnnotations = metadata.getAnnotations();
                    List<MergedAnnotation<ControllerRef>> controlleRefs = getControllerRefs(mergedAnnotations);
                    for (MergedAnnotation<ControllerRef> controllerRef : controlleRefs) {
                        registerController(registry, controllerRef.getString("value"), beanName);
                    }
                }
            }
        }
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        // DO NOTHING
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
