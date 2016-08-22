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
import com.laynemobile.proxy.functions.Action4;
import com.laynemobile.proxy.functions.Actions;

public class ProxyAction3Transform<P, T1, T2, T3>
        extends ProxyActionTransform<P, Action4<? super P, ? super T1, ? super T2, ? super T3>>
        implements Action4<P, T1, T2, T3> {
    public ProxyAction3Transform() {
        super(Actions.empty());
    }

    public ProxyAction3Transform(final Action0 action) {
        super(new Action4<P, T1, T2, T3>() {
            @Override public void call(P p, T1 t1, T2 t2, T3 t3) {
                action.call();
            }
        });
    }

    public ProxyAction3Transform(Action4<? super P, ? super T1, ? super T2, ? super T3> action) {
        super(action);
    }

    public ProxyAction3Transform(ProxyAction3Transform<? super P, ? super T1, ? super T2, ? super T3> action) {
        super(action.function);
    }

    @SuppressWarnings("unchecked")
    @Override protected final void invoke(P proxy, Object... args) {
        if (args.length != 3) {
            throw new RuntimeException("Action3 expecting 3 arguments.");
        }
        function.call(proxy, (T1) args[0], (T2) args[1], (T3) args[2]);
    }

    @Override public final void call(P proxy, T1 t1, T2 t2, T3 t3) {
        function.call(proxy, t1, t2, t3);
    }
}
