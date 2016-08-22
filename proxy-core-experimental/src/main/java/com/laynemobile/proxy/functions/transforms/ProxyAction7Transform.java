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
import com.laynemobile.proxy.functions.Action8;
import com.laynemobile.proxy.functions.Actions;

public class ProxyAction7Transform<P, T1, T2, T3, T4, T5, T6, T7>
        extends ProxyActionTransform<P, Action8<? super P, ? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6, ? super T7>>
        implements Action8<P, T1, T2, T3, T4, T5, T6, T7> {
    public ProxyAction7Transform() {
        super(Actions.empty());
    }

    public ProxyAction7Transform(final Action0 action) {
        super(new Action8<P, T1, T2, T3, T4, T5, T6, T7>() {
            @Override public void call(P p, T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6, T7 t7) {
                action.call();
            }
        });
    }

    public ProxyAction7Transform(
            Action8<? super P, ? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6, ? super T7> action) {
        super(action);
    }

    public ProxyAction7Transform(
            ProxyAction7Transform<? super P, ? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6, ? super T7> action) {
        super(action.function);
    }

    @SuppressWarnings("unchecked")
    @Override protected final void invoke(P proxy, Object... args) {
        if (args.length != 7) {
            throw new RuntimeException("Action7 expecting 7 arguments.");
        }
        function.call(proxy, (T1) args[0], (T2) args[1], (T3) args[2], (T4) args[3], (T5) args[4], (T6) args[5],
                (T7) args[6]);
    }

    @Override public final void call(P proxy, T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6, T7 t7) {
        function.call(proxy, t1, t2, t3, t4, t5, t6, t7);
    }
}
