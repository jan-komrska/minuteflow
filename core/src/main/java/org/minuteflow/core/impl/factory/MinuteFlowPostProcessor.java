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
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.minuteflow.core.api.annotation.ControllerRef;
import org.minuteflow.core.api.annotation.ControllerRefType;
import org.minuteflow.core.api.annotation.ControllerRefs;
import org.minuteflow.core.api.annotation.MinuteEntityRef;
import org.minuteflow.core.api.annotation.MinuteEntityRefs;
import org.minuteflow.core.api.annotation.MinuteServiceRef;
import org.minuteflow.core.api.annotation.MinuteServiceRefs;
import org.minuteflow.core.api.bean.BaseController;
import org.minuteflow.core.api.bean.BaseSourceResolver;
import org.minuteflow.core.api.bean.DispatchProxyFactory;
import org.minuteflow.core.api.bean.ExpressionState;
import org.minuteflow.core.api.bean.ExpressionStateType;
import org.minuteflow.core.api.contract.Dispatcher;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.annotation.Scope;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.data.repository.CrudRepository;
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
        String expressionStateName = nextBeanName(parentBeanName, "expression-state");
        //
        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(ExpressionState.class);
        beanDefinitionBuilder.setScope(BeanDefinition.SCOPE_SINGLETON);
        beanDefinitionBuilder.setLazyInit(false);
        beanDefinitionBuilder.addPropertyValue("type", type);
        beanDefinitionBuilder.addPropertyValue("targetStateNames", targetStateNames);
        //
        registry.registerBeanDefinition(expressionStateName, beanDefinitionBuilder.getBeanDefinition());
        //
        log.debug("Registered expression state [" + expressionStateName + "]");
        return expressionStateName;
    }

    private String registerController(BeanDefinitionRegistry registry, String parentBeanName, MergedAnnotation<ControllerRef> controllerRef) {
        String controllerName = nextBeanName(parentBeanName, "controller");
        ControllerRefType type = controllerRef.getEnum("type", ControllerRefType.class);
        String[] targetStateNames = controllerRef.getStringArray("value");
        if (ArrayUtils.isEmpty(targetStateNames)) {
            throw new IllegalArgumentException();
        }
        //
        String parentStateName = switch (type) {
            case IDENTITY -> targetStateNames[0];
            default -> registerExpressionState(registry, parentBeanName, ExpressionStateType.valueOf(type), targetStateNames);
        };
        String serviceName = parentBeanName;
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
        return controllerName;
    }

    private String registerDispatchProxy(BeanDefinitionRegistry registry, String parentBeanName, MergedAnnotation<MinuteServiceRef> minuteServiceRef) {
        String dispatchProxyName = nextBeanName(parentBeanName, "dispatch-proxy");
        Class<?> serviceClass = minuteServiceRef.getClass("serviceClass");
        String staticState = minuteServiceRef.getString("staticState");
        Class<?>[] rollbackFor = minuteServiceRef.getClassArray("rollbackFor");
        //
        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(DispatchProxyFactory.class);
        beanDefinitionBuilder.setScope(BeanDefinition.SCOPE_SINGLETON);
        beanDefinitionBuilder.setPrimary(true);
        beanDefinitionBuilder.setLazyInit(false);
        beanDefinitionBuilder.addConstructorArgValue(serviceClass);
        beanDefinitionBuilder.addPropertyValue("dispatcher", new RuntimeBeanReference(Dispatcher.class));
        //
        if (StringUtils.isNotEmpty(staticState)) {
            beanDefinitionBuilder.addPropertyReference("staticState", staticState);
        }
        if (ArrayUtils.isNotEmpty(rollbackFor)) {
            beanDefinitionBuilder.addPropertyValue("rollbackFor", rollbackFor);
        }
        //
        registry.registerBeanDefinition(dispatchProxyName, beanDefinitionBuilder.getBeanDefinition());
        //
        log.debug("Registered dispatch proxy [" + dispatchProxyName + "]");
        return dispatchProxyName;
    }

    private String registerStateAccessor(BeanDefinitionRegistry registry, String parentBeanName, MergedAnnotation<MinuteEntityRef> minuteEntityRef) {
        String stateAccessorName = nextBeanName(parentBeanName, "state-accessor");
        Class<?> entityClass = minuteEntityRef.getClass("entityClass");
        String[] statePatterns = minuteEntityRef.getStringArray("statePattern");
        //
        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(MinuteFlowStateAccessor.class);
        beanDefinitionBuilder.setScope(BeanDefinition.SCOPE_SINGLETON);
        beanDefinitionBuilder.setLazyInit(false);
        beanDefinitionBuilder.addConstructorArgValue(entityClass);
        beanDefinitionBuilder.addConstructorArgValue(statePatterns);
        //
        registry.registerBeanDefinition(stateAccessorName, beanDefinitionBuilder.getBeanDefinition());
        //
        log.debug("Registered state accessor [" + stateAccessorName + "]");
        return stateAccessorName;
    }

    private String registerSourceResolver(BeanDefinitionRegistry registry, String parentBeanName, MergedAnnotation<MinuteEntityRef> minuteEntityRef) {
        String sourceResolverName = nextBeanName(parentBeanName, "source-resolver");
        Class<?> entityClass = minuteEntityRef.getClass("entityClass");
        Class<?> repositoryClass = minuteEntityRef.getClass("repositoryClass");
        String defaultFindMethod = minuteEntityRef.getString("defaultFindMethod");
        //
        if (!ClassUtils.isAssignable(repositoryClass, CrudRepository.class)) {
            return null;
        }
        //
        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(BaseSourceResolver.class);
        beanDefinitionBuilder.setScope(BeanDefinition.SCOPE_SINGLETON);
        beanDefinitionBuilder.setLazyInit(false);
        beanDefinitionBuilder.addConstructorArgValue(entityClass);
        beanDefinitionBuilder.addConstructorArgValue(new RuntimeBeanReference(repositoryClass));
        beanDefinitionBuilder.addConstructorArgValue(defaultFindMethod);
        //
        registry.registerBeanDefinition(sourceResolverName, beanDefinitionBuilder.getBeanDefinition());
        //
        log.debug("Registered source resolver [" + sourceResolverName + "]");
        return sourceResolverName;
    }

    //

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

    private MergedAnnotations getMetadata(BeanDefinitionRegistry registry, String beanName) {
        BeanDefinition abstractBeanDefinition = registry.getBeanDefinition(beanName);
        //
        AnnotatedTypeMetadata metadata = null;
        if (abstractBeanDefinition instanceof AnnotatedBeanDefinition beanDefinition) {
            if (StringUtils.isNotEmpty(beanDefinition.getFactoryMethodName())) {
                metadata = beanDefinition.getFactoryMethodMetadata();
            } else {
                metadata = beanDefinition.getMetadata();
            }
        }
        //
        if (metadata != null) {
            return metadata.getAnnotations();
        } else {
            return MergedAnnotations.of(Collections.emptyList());
        }
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        String[] beanNames = ArrayUtils.nullToEmpty(registry.getBeanDefinitionNames());
        for (String beanName : beanNames) {
            MergedAnnotations mergedAnnotations = getMetadata(registry, beanName);
            //
            List<MergedAnnotation<ControllerRef>> controlleRefs = //
                    getAnnotations(mergedAnnotations, ControllerRef.class, ControllerRefs.class);
            for (MergedAnnotation<ControllerRef> controllerRef : controlleRefs) {
                registerController(registry, beanName, controllerRef);
            }
            //
            List<MergedAnnotation<MinuteServiceRef>> minuteServiceRefs = //
                    getAnnotations(mergedAnnotations, MinuteServiceRef.class, MinuteServiceRefs.class);
            for (MergedAnnotation<MinuteServiceRef> minuteServiceRef : minuteServiceRefs) {
                registerDispatchProxy(registry, beanName, minuteServiceRef);
            }
            //
            List<MergedAnnotation<MinuteEntityRef>> minuteEntityRefs = //
                    getAnnotations(mergedAnnotations, MinuteEntityRef.class, MinuteEntityRefs.class);
            for (MergedAnnotation<MinuteEntityRef> minuteEntityRef : minuteEntityRefs) {
                registerStateAccessor(registry, beanName, minuteEntityRef);
                registerSourceResolver(registry, beanName, minuteEntityRef);
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
