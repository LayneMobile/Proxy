/*
 * Copyright 2016 Layne Mobile, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.laynemobile.api.playground;

import com.laynemobile.api.Params;
import com.laynemobile.proxy.TypeToken;
import com.laynemobile.proxy.functions.ConcreteActionDef;

import rx.Subscriber;

public class SourceDef_call__P_Subscriber<T, P extends Params> extends ConcreteActionDef {
    public SourceDef_call__P_Subscriber() {
        this(defaultParamTypes());
    }

    protected SourceDef_call__P_Subscriber(TypeToken<? extends P> p,
            TypeToken<? extends Subscriber<? super T>> subscriber) {
        this(new TypeToken<?>[]{p, subscriber});
    }

    private SourceDef_call__P_Subscriber(TypeToken<?>[] paramTypes) {
        super("call", paramTypes);
    }

    private static <T> TypeToken<?>[] defaultParamTypes() {
        return new TypeToken<?>[]{
                TypeToken.get(Params.class),
                new TypeToken<Subscriber<? super T>>() {}
        };
    }
}
