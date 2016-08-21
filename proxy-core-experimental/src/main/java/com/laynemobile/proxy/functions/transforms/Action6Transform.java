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
import com.laynemobile.proxy.functions.Action6;
import com.laynemobile.proxy.functions.Actions;

public class Action6Transform<T1, T2, T3, T4, T5, T6>
        extends ActionTransform<Action6<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6>>
        implements Action6<T1, T2, T3, T4, T5, T6> {

    public Action6Transform() {
        super(Actions.empty());
    }

    public Action6Transform(Action6<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6> action) {
        super(action);
    }

    public Action6Transform(
            Action6Transform<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6> action) {
        super(action.function);
    }

    public Action6Transform(final Action0 action) {
        super(new Action6<T1, T2, T3, T4, T5, T6>() {
            @Override public void call(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6) {
                action.call();
            }
        });
    }

    @SuppressWarnings("unchecked")
    @Override protected final void invoke(Object... args) {
        if (args.length != 6) {
            throw new RuntimeException("Action6 expecting 6 arguments.");
        }
        function.call((T1) args[0], (T2) args[1], (T3) args[2], (T4) args[3], (T5) args[4], (T6) args[5]);
    }

    @Override public final void call(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6) {
        function.call(t1, t2, t3, t4, t5, t6);
    }
}
