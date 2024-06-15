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

import org.apache.commons.lang3.ArrayUtils;
import org.minuteflow.core.api.annotation.ControllerRef;
import org.minuteflow.core.api.annotation.ControllerRefs;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotations;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MinuteFlowPostProcessor implements BeanDefinitionRegistryPostProcessor {
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

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        String[] beanNames = ArrayUtils.nullToEmpty(registry.getBeanDefinitionNames());
        for (String beanName : beanNames) {
            BeanDefinition abstractBeanDefinition = registry.getBeanDefinition(beanName);
            if (abstractBeanDefinition instanceof AnnotatedBeanDefinition beanDefinition) {
                MergedAnnotations mergedAnnotations = beanDefinition.getMetadata().getAnnotations();
                List<MergedAnnotation<ControllerRef>> controlleRefs = getControllerRefs(mergedAnnotations);
            }
        }
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        // DO NOTHING
    }
}
