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

package com.laynemobile.proxy.functions;

import com.laynemobile.proxy.TypeToken;
import com.laynemobile.proxy.functions.transforms.Action9Transform;

public class Action9Def<T1, T2, T3, T4, T5, T6, T7, T8, T9> extends ActionDef<Action9Transform<T1, T2, T3, T4, T5, T6, T7, T8, T9>> {
    public Action9Def(String name, TypeToken<T1> t1, TypeToken<T2> t2, TypeToken<T3> t3, TypeToken<T4> t4,
            TypeToken<T5> t5, TypeToken<T6> t6, TypeToken<T7> t7, TypeToken<T8> t8, TypeToken<T9> t9) {
        super(name, new TypeToken<?>[]{t1, t2, t3, t4, t5, t6, t7, t8, t9});
    }

    @Override public Action<T1, T2, T3, T4, T5, T6, T7, T8, T9> asFunction(
            Action9Transform<T1, T2, T3, T4, T5, T6, T7, T8, T9> transform) {
        return new Action<>(this, transform);
    }

    public static class Action<T1, T2, T3, T4, T5, T6, T7, T8, T9> extends ProxyAction<Action9Transform<T1, T2, T3, T4, T5, T6, T7, T8, T9>> {
        protected Action(Action9Def<T1, T2, T3, T4, T5, T6, T7, T8, T9> actionDef,
                Action9Transform<T1, T2, T3, T4, T5, T6, T7, T8, T9> action) {
            super(actionDef, action);
        }
    }
}
