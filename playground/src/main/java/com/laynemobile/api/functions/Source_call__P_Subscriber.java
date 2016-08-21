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

package com.laynemobile.api.functions;

import com.laynemobile.api.Params;
import com.laynemobile.proxy.TypeToken;
import com.laynemobile.proxy.functions.AbstractProxyAction;
import com.laynemobile.proxy.functions.Action0;
import com.laynemobile.proxy.functions.Action2;
import com.laynemobile.proxy.functions.ConcreteActionDef;
import com.laynemobile.proxy.functions.transforms.Action2Transform;

import rx.Subscriber;

public class Source_call__P_Subscriber<T, P extends Params> extends AbstractProxyAction<Action2Transform<P, Subscriber<? super T>>> {
    public Source_call__P_Subscriber(Source_call__P_Subscriber<T, P> proxyFunction) {
        super(proxyFunction);
    }

    public Source_call__P_Subscriber(Def<T, P> functionDef, Transform<T, P> action) {
        super(functionDef, action);
    }

    public Source_call__P_Subscriber(Transform<T, P> action) {
        super(new Def<>(), action);
    }

    public static class Def<T, P extends Params> extends ConcreteActionDef {
        public Def() {
            super("call", new TypeToken<?>[]{
                    new TypeToken<P>() {},
                    new TypeToken<Subscriber<? super T>>() {}
            });
        }
    }

    public static class Transform<T, P extends Params> extends Action2Transform<P, Subscriber<? super T>> {
        public Transform(Action2<? super P, ? super Subscriber<? super T>> action) {
            super(action);
        }

        public Transform(Action0 action) {
            super(action);
        }
    }
}
