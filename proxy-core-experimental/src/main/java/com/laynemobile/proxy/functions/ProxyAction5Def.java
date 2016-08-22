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
import com.laynemobile.proxy.functions.transforms.ProxyAction5Transform;

public class ProxyAction5Def<P, T1, T2, T3, T4, T5> extends ProxyActionDef<P, ProxyAction5Transform<P, T1, T2, T3, T4, T5>> {
    public ProxyAction5Def(String name, TypeToken<T1> t1, TypeToken<T2> t2, TypeToken<T3> t3, TypeToken<T4> t4,
            TypeToken<T5> t5) {
        super(name, new TypeToken<?>[]{t1, t2, t3, t4, t5});
    }

    @Override public Action<P, T1, T2, T3, T4, T5> asFunction(ProxyAction5Transform<P, T1, T2, T3, T4, T5> transform) {
        return new Action<>(this, transform);
    }

    public static class Action<P, T1, T2, T3, T4, T5> extends ProxyAction2<P, ProxyAction5Transform<P, T1, T2, T3, T4, T5>> {
        protected Action(ProxyAction5Def<P, T1, T2, T3, T4, T5> actionDef,
                ProxyAction5Transform<P, T1, T2, T3, T4, T5> action) {
            super(actionDef, action);
        }
    }
}
