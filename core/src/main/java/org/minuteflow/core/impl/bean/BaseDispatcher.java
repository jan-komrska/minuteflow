package org.minuteflow.core.impl.bean;

import java.lang.reflect.InvocationHandler;

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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Predicate;

import org.apache.commons.collections4.SetUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.minuteflow.core.api.contract.Controller;
import org.minuteflow.core.api.contract.DispatchContext;
import org.minuteflow.core.api.contract.Dispatcher;
import org.minuteflow.core.api.contract.MethodDescriptor;
import org.minuteflow.core.api.contract.Source;
import org.minuteflow.core.api.contract.SourceResolver;
import org.minuteflow.core.api.contract.State;
import org.minuteflow.core.api.contract.StateManager;
import org.minuteflow.core.api.exception.ControllerNotFoundException;
import org.minuteflow.core.impl.repository.ControllerRepository;
import org.minuteflow.core.impl.repository.SourceResolverRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import lombok.Getter;

@Component
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class BaseDispatcher implements Dispatcher {
    @Getter
    private static class StateWithPath implements Comparable<StateWithPath> {
        private State state;
        private String[] path;

        public StateWithPath(State state) {
            this.state = state;
            this.path = calculatePath(state);
        }

        private String[] calculatePath(State state) {
            ArrayList<String> path = new ArrayList<String>();
            calculatePath(path, state);
            return path.toArray(new String[] {});
        }

        private void calculatePath(ArrayList<String> path, State state) {
            if (state.getParentState() != null) {
                calculatePath(path, state.getParentState());
            }
            //
            path.add(state.getName());
        }

        @Override
        public int compareTo(StateWithPath that) {
            return Arrays.compare(this.getPath(), that.getPath());
        }
    }

    //

    @Autowired
    private StateManager stateManager;

    @Autowired
    private ControllerRepository controllerRepository;

    @Autowired
    private SourceResolverRepository sourceResolverRepository;

    @Autowired
    private MethodDescriptor methodDescriptor;

    //

    public BaseDispatcher() {
    }

    //

    private Set<State> envelopeStates(Set<State> sourceStates) {
        Set<State> targetStates = new HashSet<State>();
        //
        sourceStates = SetUtils.emptyIfNull(sourceStates);
        for (State sourceState : sourceStates) {
            while (sourceState != null) {
                sourceState = stateManager.valueOf(sourceState.getName());
                // TODO check null sourceState
                targetStates.add(sourceState);
                //
                sourceState = sourceState.getParentState();
            }
        }
        //
        return targetStates;
    }

    private List<State> envelopeAndSortStates(Set<State> sourceStates) {
        TreeSet<StateWithPath> targetStates = new TreeSet<StateWithPath>();
        //
        sourceStates = envelopeStates(sourceStates);
        for (State sourceState : sourceStates) {
            targetStates.add(new StateWithPath(sourceState));
        }
        //
        return targetStates.descendingSet().stream().map(StateWithPath::getState).toList();
    }

    @SuppressWarnings("unchecked")
    private <Entity> Source<Entity> asSource(Object entity, Predicate<Source<Entity>> predicate) {
        Source<Entity> source = (entity instanceof Source<?>) ? (Source<Entity>) entity : null;
        if (source == null) {
            return null;
        } else if (predicate == null) {
            return source;
        } else if (predicate.test(source)) {
            return source;
        } else {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private <Entity> SourceResolver<Entity> getSourceResolver(Method method) {
        Class<Entity> entityClass = (Class<Entity>) methodDescriptor.getEntityClass(method);
        return (entityClass != null) ? sourceResolverRepository.getSourceResolver(entityClass) : null;
    }

    private boolean isRollbackException(Throwable throwable, Class<? extends Throwable>[] rollbackFor) {
        if ((throwable == null) || ArrayUtils.isEmpty(rollbackFor)) {
            return false;
        }
        //
        for (Class<? extends Throwable> rollbackForElement : rollbackFor) {
            if (rollbackForElement.isInstance(throwable)) {
                return true;
            }
        }
        return false;
    }

    //

    private Object resolveEntity(Method method, Object[] args) {
        Object entity = methodDescriptor.getEntity(method, args);
        Source<Object> source = asSource(entity, null);
        //
        if (source == null) {
            return entity;
        } else if (source.isResolved()) {
            return source.getEntity();
        } else {
            return null;
        }
    }

    private SourceResolver<Object> resolveSource(Method method, Object[] args) {
        Object entity = methodDescriptor.getEntity(method, args);
        Source<Object> source = asSource(entity, (s) -> !s.isResolved());
        //
        if (source != null) {
            SourceResolver<Object> sourceResolver = Objects.requireNonNull(getSourceResolver(method));
            String sourceName = StringUtils.defaultString(source.getName(), methodDescriptor.getEntityName(method));
            //
            entity = source = sourceResolver.resolve(sourceName, source.getParameters());
            methodDescriptor.setEntity(method, args, entity);
            //
            return sourceResolver;
        } else {
            return null;
        }
    }

    private void commitSource(Method method, Object[] args, SourceResolver<Object> sourceResolver) {
        Object entity = methodDescriptor.getEntity(method, args);
        Source<Object> source = asSource(entity, (s) -> s.isResolved() && !s.isForRollback());
        //
        if ((sourceResolver != null) && (source != null)) {
            sourceResolver.commit(source);
        }
    }

    private void rollbackSource(Method method, Object[] args, Throwable throwable, DispatchContext dispatchContext) {
        Object entity = methodDescriptor.getEntity(method, args);
        Source<Object> source = asSource(entity, (s) -> s.isResolved());
        boolean rollbackFlag = isRollbackException(throwable, dispatchContext.getRollbackFor());
        //
        if ((source != null) && rollbackFlag) {
            source.markForRollback();
        }
    }

    private Controller resolveController(Method method, Object entity, DispatchContext dispatchContext) {
        String actionName = methodDescriptor.getActionName(method);
        //
        if (methodDescriptor.isStaticAction(method)) {
            State state = Objects.requireNonNull(dispatchContext.getStaticState());
            return controllerRepository.getController(state.getName(), actionName);
        }
        //
        List<State> states = (entity != null) ? envelopeAndSortStates(stateManager.getStates(entity)) : List.of();
        for (State state : states) {
            Controller controller = controllerRepository.getController(state.getName(), actionName);
            if (controller != null) {
                return controller;
            }
        }
        //
        return null;
    }

    @Override
    public Object dispatch(Method method, Object[] args, DispatchContext dispatchContext) throws Throwable {
        args = ArrayUtils.nullToEmpty(args);
        args = Arrays.copyOf(args, args.length);
        //
        SourceResolver<Object> sourceResolver = resolveSource(method, args);
        Object entity = resolveEntity(method, args);
        Controller controller = resolveController(method, entity, dispatchContext);
        //
        if (controller != null) {
            String actionName = methodDescriptor.getActionName(method);
            try {
                return controller.executeAction(actionName, args);
            } catch (Throwable throwable) {
                rollbackSource(method, args, throwable, dispatchContext);
                throw throwable;
            } finally {
                commitSource(method, args, sourceResolver);
            }
        } else if ((dispatchContext.getProxy() != null) && method.isDefault()) {
            return InvocationHandler.invokeDefault(dispatchContext.getProxy(), method, args);
        } else {
            throw new ControllerNotFoundException();
        }
    }
}
