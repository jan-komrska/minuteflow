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

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.minuteflow.core.api.annotation.ControllerRef;
import org.minuteflow.core.api.annotation.ControllerRefType;
import org.minuteflow.core.api.annotation.ControllerRefs;
import org.minuteflow.core.api.annotation.MinuteEntityRef;
import org.minuteflow.core.api.annotation.MinuteEntityRefs;
import org.minuteflow.core.api.annotation.MinuteServiceRef;
import org.minuteflow.core.api.annotation.MinuteServiceRefs;
import org.minuteflow.core.api.bean.BaseController;
import org.minuteflow.core.api.bean.DispatchProxyFactory;
import org.minuteflow.core.api.bean.ExpressionState;
import org.minuteflow.core.api.bean.ExpressionStateType;
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

    public String nextBeanName(String parentBeanName, String type) {
        return parentBeanName + ":" + type + ":" + beanNameSequence.addAndGet(1);
    }

    //

    private String registerExpressionState(BeanDefinitionRegistry registry, String parentBeanName, ExpressionStateType type, String[] targetStateNames) {
        String stateName = nextBeanName(parentBeanName, "expression-state");
        //
        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(ExpressionState.class);
        beanDefinitionBuilder.setScope(BeanDefinition.SCOPE_SINGLETON);
        beanDefinitionBuilder.setLazyInit(false);
        beanDefinitionBuilder.addPropertyValue("type", type);
        beanDefinitionBuilder.addPropertyValue("targetStateNames", targetStateNames);
        //
        registry.registerBeanDefinition(stateName, beanDefinitionBuilder.getBeanDefinition());
        //
        log.debug("Registered expression state [" + stateName + "]");
        //
        return stateName;
    }

    private String registerController(BeanDefinitionRegistry registry, String parentBeanName, String parentStateName, String serviceName) {
        String controllerName = nextBeanName(parentBeanName, "controller");
        //
        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(BaseController.class);
        beanDefinitionBuilder.setScope(BeanDefinition.SCOPE_SINGLETON);
        beanDefinitionBuilder.setLazyInit(false);
        beanDefinitionBuilder.addPropertyReference("parentState", parentStateName);
        beanDefinitionBuilder.addPropertyReference("service", serviceName);
        //
        registry.registerBeanDefinition(controllerName, beanDefinitionBuilder.getBeanDefinition());
        //
        log.debug("Registered controller [" + controllerName + "]");
        //
        return controllerName;
    }

    private String registerMinuteService(BeanDefinitionRegistry registry, String parentBeanName, Class<?> serviceClass) {
        String minuteServiceName = nextBeanName(parentBeanName, "minute-service");
        //
        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(DispatchProxyFactory.class);
        beanDefinitionBuilder.setScope(BeanDefinition.SCOPE_SINGLETON);
        beanDefinitionBuilder.setPrimary(true);
        beanDefinitionBuilder.setLazyInit(false);
        beanDefinitionBuilder.addConstructorArgValue(serviceClass);
        beanDefinitionBuilder.addConstructorArgReference("org.minuteflow.core.impl.bean.BaseDispatcher");
        //
        registry.registerBeanDefinition(minuteServiceName, beanDefinitionBuilder.getBeanDefinition());
        //
        log.debug("Registered minute service [" + minuteServiceName + "]");
        //
        return minuteServiceName;
    }

    private String registerMinuteEntity(BeanDefinitionRegistry registry, String parentBeanName, Class<?> entityClass, String[] statePatterns) {
        String minuteEntityName = nextBeanName(parentBeanName, "minute-entity");
        //
        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(MinuteFlowStateAccessor.class);
        beanDefinitionBuilder.setScope(BeanDefinition.SCOPE_SINGLETON);
        beanDefinitionBuilder.setLazyInit(false);
        beanDefinitionBuilder.addConstructorArgValue(entityClass);
        beanDefinitionBuilder.addConstructorArgValue(statePatterns);
        //
        registry.registerBeanDefinition(minuteEntityName, beanDefinitionBuilder.getBeanDefinition());
        //
        log.debug("Registered minute entity [" + minuteEntityName + "]");
        //
        return minuteEntityName;
    }

    private <TargetAnnotation extends Annotation> List<MergedAnnotation<TargetAnnotation>> getAnnotations( //
            MergedAnnotations mergedAnnotations, Class<TargetAnnotation> targetAnnotationClass, //
            Class<? extends Annotation> targetRepeatableAnnotationClass) {
        ArrayList<MergedAnnotation<TargetAnnotation>> annotationList = new ArrayList<MergedAnnotation<TargetAnnotation>>();
        //
        mergedAnnotations.stream(targetRepeatableAnnotationClass).flatMap((repeatableAnnotation) -> {
            return Arrays.stream(repeatableAnnotation.getAnnotationArray("value", targetAnnotationClass));
        }).forEach(annotationList::add);
        //
        mergedAnnotations.stream(targetAnnotationClass).forEach(annotationList::add);
        //
        return annotationList;
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        String[] beanNames = ArrayUtils.nullToEmpty(registry.getBeanDefinitionNames());
        for (String beanName : beanNames) {
            BeanDefinition abstractBeanDefinition = registry.getBeanDefinition(beanName);
            if (abstractBeanDefinition instanceof AnnotatedBeanDefinition beanDefinition) {
                AnnotatedTypeMetadata metadata;
                if (StringUtils.isNotEmpty(beanDefinition.getFactoryMethodName())) {
                    metadata = beanDefinition.getFactoryMethodMetadata();
                } else {
                    metadata = beanDefinition.getMetadata();
                }
                //
                if (metadata != null) {
                    MergedAnnotations mergedAnnotations = metadata.getAnnotations();
                    //
                    List<MergedAnnotation<ControllerRef>> controlleRefs = //
                            getAnnotations(mergedAnnotations, ControllerRef.class, ControllerRefs.class);
                    for (MergedAnnotation<ControllerRef> controllerRef : controlleRefs) {
                        ControllerRefType type = controllerRef.getEnum("type", ControllerRefType.class);
                        String[] targetStateNames = controllerRef.getStringArray("value");
                        //
                        if (ControllerRefType.IDENTITY.equals(type)) {
                            if (targetStateNames.length == 1) {
                                registerController(registry, beanName, targetStateNames[0], beanName);
                            } else {
                                throw new IllegalArgumentException();
                            }
                        } else {
                            String stateName = registerExpressionState(registry, beanName, ExpressionStateType.valueOf(type), targetStateNames);
                            registerController(registry, beanName, stateName, beanName);
                        }
                    }
                    //
                    List<MergedAnnotation<MinuteServiceRef>> minuteServiceRefs = //
                            getAnnotations(mergedAnnotations, MinuteServiceRef.class, MinuteServiceRefs.class);
                    for (MergedAnnotation<MinuteServiceRef> minuteServiceRef : minuteServiceRefs) {
                        Class<?> serviceClass = minuteServiceRef.getClass("value");
                        registerMinuteService(registry, beanName, serviceClass);
                    }
                    //
                    List<MergedAnnotation<MinuteEntityRef>> minuteEntityRefs = //
                            getAnnotations(mergedAnnotations, MinuteEntityRef.class, MinuteEntityRefs.class);
                    for (MergedAnnotation<MinuteEntityRef> minuteEntityRef : minuteEntityRefs) {
                        Class<?> entityClass = minuteEntityRef.getClass("entityClass");
                        String[] statePatterns = minuteEntityRef.getStringArray("statePattern");
                        registerMinuteEntity(registry, beanName, entityClass, statePatterns);
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
