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

import org.minuteflow.core.api.contract.CalculatedState;
import org.minuteflow.core.api.contract.State;

public class BaseCalculatedState extends BaseState implements CalculatedState {
    public BaseCalculatedState() {
        super();
    }

    public BaseCalculatedState(String name) {
        super(name);
    }

    public BaseCalculatedState(String name, State parentState) {
        super(name, parentState);
    }
}
