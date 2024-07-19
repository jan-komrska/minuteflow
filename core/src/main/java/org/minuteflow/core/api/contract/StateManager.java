package org.minuteflow.core.api.contract;

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

import java.util.Set;

import org.minuteflow.core.api.exception.BaseException;

public interface StateManager extends StateCollection {
    public Set<State> getStates(Object entity) throws BaseException;

    public void setStates(Object entity, Set<State> states) throws BaseException;

    public void setStates(Object entity, State... states) throws BaseException;

    public boolean containsState(Object entity, State... states) throws BaseException;

    public void addState(Object entity, State... states) throws BaseException;

    public void removeState(Object entity, State... states) throws BaseException;

    public void updateState(Object entity, State sourceState, State targetState) throws BaseException;

    public void updateState(Object entity, State[] sourceStates, State[] targetStates) throws BaseException;
}
