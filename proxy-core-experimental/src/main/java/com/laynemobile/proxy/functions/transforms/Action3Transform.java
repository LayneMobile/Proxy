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
import com.laynemobile.proxy.functions.Action3;
import com.laynemobile.proxy.functions.ActionTransform;

public class Action3Transform<T1, T2, T3>
        extends ActionTransform<Action3<? super T1, ? super T2, ? super T3>>
        implements Action3<T1, T2, T3> {
    public Action3Transform(Action3<? super T1, ? super T2, ? super T3> action) {
        super(action);
    }

    public Action3Transform(final Action0 action) {
        super(new Action3<T1, T2, T3>() {
            @Override public void call(T1 t1, T2 t2, T3 t3) {
                action.call();
            }
        });
    }

    @Override public void call(T1 t1, T2 t2, T3 t3) {
        function.call(t1, t2, t3);
    }
}
