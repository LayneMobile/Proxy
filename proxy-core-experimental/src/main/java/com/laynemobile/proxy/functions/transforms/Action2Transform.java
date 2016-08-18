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
import com.laynemobile.proxy.functions.ActionTransform;

public class Action2Transform<T1, T2>
        extends ActionTransform<Action2<? super T1, ? super T2>>
        implements Action2<T1, T2> {

    public Action2Transform(Action2<? super T1, ? super T2> action) {
        super(action);
    }

    public Action2Transform(final Action0 action) {
        super(new Action2<T1, T2>() {
            @Override public void call(T1 t1, T2 t2) {
                action.call();
            }
        });
    }

    @Override public final void call(T1 t1, T2 t2) {
        function.call(t1, t2);
    }
}
