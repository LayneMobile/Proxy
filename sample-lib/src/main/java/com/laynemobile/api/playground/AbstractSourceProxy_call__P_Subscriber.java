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
import com.laynemobile.proxy.annotations.Generated;
import com.laynemobile.proxy.functions.AbstractProxyAction;
import com.laynemobile.proxy.functions.Action2;
import com.laynemobile.proxy.functions.Actions;
import com.laynemobile.proxy.functions.FuncN;
import com.laynemobile.proxy.functions.Functions;

import rx.Subscriber;

@Generated
public abstract class AbstractSourceProxy_call__P_Subscriber<T, P extends Params>
        extends AbstractProxyAction<Action2<P, Subscriber<? super T>>> {
    protected AbstractSourceProxy_call__P_Subscriber(SourceDef_call__P_Subscriber<? extends T, ? extends P> functionDef,
            Action2<P, Subscriber<? super T>> action) {
        super(functionDef, action);
    }

    protected AbstractSourceProxy_call__P_Subscriber(Action2<P, Subscriber<? super T>> action) {
        super(new SourceDef_call__P_Subscriber<>(), action);
    }

    @Override protected FuncN<Void> toFuncN(Action2<P, Subscriber<? super T>> function) {
        return Functions.fromFunc(Actions.toFunc(function));
    }
}
