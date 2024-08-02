package org.minuteflow.tstapp.multi;

/*-
 * ========================LICENSE_START=================================
 * minuteflow-test-application
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

import org.minuteflow.core.api.annotation.EntityRef;
import org.minuteflow.core.api.contract.Source;

public interface OrderManager {
    public default Long createOrder(String name) {
        throw new UnsupportedOperationException();
    }

    public default void startOrder(@EntityRef Source<OrderEntity> order) {
        throw new UnsupportedOperationException();
    }

    public default void finishOrder(@EntityRef Source<OrderEntity> order) {
        // DO NOTHING
    }

    public default void orderPaymentDone(@EntityRef Source<OrderEntity> order) {
        throw new UnsupportedOperationException();
    }

    public default void orderPackagingDone(@EntityRef Source<OrderEntity> order) {
        throw new UnsupportedOperationException();
    }
}
