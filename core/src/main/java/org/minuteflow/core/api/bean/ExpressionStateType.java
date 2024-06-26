package org.minuteflow.core.api.bean;

import org.minuteflow.core.api.annotation.ControllerRefType;

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

public enum ExpressionStateType {
    AND(true, false, false), NAND(true, false, true), //
    OR(false, true, false), NOR(false, true, true);

    //

    private boolean tagAnd = false;
    private boolean tagOr = false;
    private boolean tagNot = false;

    private ExpressionStateType(boolean tagAnd, boolean tagOr, boolean tagNot) {
        this.tagAnd = tagAnd;
        this.tagOr = tagOr;
        this.tagNot = tagNot;
    }

    public boolean hasTagAnd() {
        return tagAnd;
    }

    public boolean hasTagOr() {
        return tagOr;
    }

    public boolean hasTagNot() {
        return tagNot;
    }

    //

    public static ExpressionStateType valueOf(ControllerRefType type) {
        return switch (type) {
            case AND -> ExpressionStateType.AND;
            case NAND -> ExpressionStateType.NAND;
            case OR -> ExpressionStateType.OR;
            case NOR -> ExpressionStateType.NOR;
            case IDENTITY -> null;
        };
    }
}
