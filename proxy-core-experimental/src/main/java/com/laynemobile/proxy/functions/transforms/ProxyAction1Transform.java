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

package com.laynemobile.proxy.functions.transforms;

import com.laynemobile.proxy.functions.Action0;
import com.laynemobile.proxy.functions.Action2;
import com.laynemobile.proxy.functions.Actions;

public class ProxyAction1Transform<P, T>
        extends ProxyActionTransform<P, Action2<? super P, ? super T>>
        implements Action2<P, T> {
    public ProxyAction1Transform() {
        super(Actions.empty());
    }

    public ProxyAction1Transform(final Action0 action) {
        super(new Action2<P, T>() {
            @Override public void call(P p, T t) {
                action.call();
            }
        });
    }

    public ProxyAction1Transform(Action2<? super P, ? super T> action) {
        super(action);
    }

    public ProxyAction1Transform(ProxyAction1Transform<? super P, ? super T> action) {
        super(action.function);
    }

    @SuppressWarnings("unchecked")
    @Override protected final void invoke(P proxy, Object... args) {
        if (args.length != 1) {
            throw new RuntimeException("Action1 expecting 1 arguments.");
        }
        function.call(proxy, (T) args[0]);
    }

    @Override public final void call(P proxy, T t) {
        function.call(proxy, t);
    }
}
