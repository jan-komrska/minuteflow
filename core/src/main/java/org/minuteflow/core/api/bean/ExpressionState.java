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

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.SetUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.minuteflow.core.api.contract.State;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExpressionState extends BaseCalculatedState {
    private ExpressionStateType type;
    private String[] targetStateNames;

    //

    public ExpressionState() {
        setPredicate((sourceStates) -> {
            return checkStates(sourceStates, getType(), getTargetStateNames());
        });
    }

    public ExpressionState(ExpressionStateType type, String[] targetStateNames) {
        this();
        //
        setType(type);
        setTargetStateNames(targetStateNames);
    }

    //

    private static boolean checkState(Set<String> sourceStateNames, String targetStateName) {
        targetStateName = StringUtils.defaultString(targetStateName);
        //
        boolean negation = targetStateName.startsWith("!");
        targetStateName = (negation) ? targetStateName.substring(1) : targetStateName;
        boolean exists = sourceStateNames.contains(targetStateName);
        return (negation) ? !exists : exists;
    }

    private static boolean checkStates(Set<State> sourceStates, ExpressionStateType type, String[] targetStateNames) {
        type = Objects.requireNonNull(type);
        targetStateNames = ArrayUtils.nullToEmpty(targetStateNames);
        //
        Set<String> sourceStateNames = SetUtils.emptyIfNull(sourceStates).stream(). //
                map(State::getName).collect(Collectors.toUnmodifiableSet());
        //
        boolean result = (type.hasTagAnd()) ? true : false;
        for (String targetStateName : targetStateNames) {
            if (type.hasTagAnd()) {
                result = result && checkState(sourceStateNames, targetStateName);
            } else {
                result = result || checkState(sourceStateNames, targetStateName);
            }
        }
        return (type.hasTagNot()) ? !result : result;
    }
}
