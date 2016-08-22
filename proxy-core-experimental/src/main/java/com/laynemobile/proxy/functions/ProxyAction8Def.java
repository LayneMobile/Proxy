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
import com.laynemobile.proxy.functions.transforms.ProxyAction8Transform;

public class ProxyAction8Def<P, T1, T2, T3, T4, T5, T6, T7, T8> extends ProxyActionDef<P, ProxyAction8Transform<P, T1, T2, T3, T4, T5, T6, T7, T8>> {
    public ProxyAction8Def(String name, TypeToken<T1> t1, TypeToken<T2> t2, TypeToken<T3> t3, TypeToken<T4> t4,
            TypeToken<T5> t5, TypeToken<T6> t6, TypeToken<T7> t7, TypeToken<T8> t8) {
        super(name, new TypeToken<?>[]{t1, t2, t3, t4, t5, t6, t7, t8});
    }

    @Override public Action<P, T1, T2, T3, T4, T5, T6, T7, T8> asFunction(
            ProxyAction8Transform<P, T1, T2, T3, T4, T5, T6, T7, T8> transform) {
        return new Action<>(this, transform);
    }

    public static class Action<P, T1, T2, T3, T4, T5, T6, T7, T8> extends ProxyAction2<P, ProxyAction8Transform<P, T1, T2, T3, T4, T5, T6, T7, T8>> {
        protected Action(ProxyAction8Def<P, T1, T2, T3, T4, T5, T6, T7, T8> actionDef,
                ProxyAction8Transform<P, T1, T2, T3, T4, T5, T6, T7, T8> action) {
            super(actionDef, action);
        }
    }
}
