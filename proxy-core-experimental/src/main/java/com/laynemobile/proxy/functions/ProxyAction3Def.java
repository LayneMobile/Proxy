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
import com.laynemobile.proxy.functions.transforms.ProxyAction3Transform;

public class ProxyAction3Def<P, T1, T2, T3> extends ProxyActionDef<P, ProxyAction3Transform<P, T1, T2, T3>> {
    public ProxyAction3Def(String name, TypeToken<T1> t1, TypeToken<T2> t2, TypeToken<T3> t3) {
        super(name, new TypeToken<?>[]{t1, t2, t3});
    }

    @Override public Action<P, T1, T2, T3> asFunction(ProxyAction3Transform<P, T1, T2, T3> transform) {
        return new Action<>(this, transform);
    }

    public static class Action<P, T1, T2, T3> extends ProxyAction2<P, ProxyAction3Transform<P, T1, T2, T3>> {
        protected Action(ProxyAction3Def<P, T1, T2, T3> actionDef, ProxyAction3Transform<P, T1, T2, T3> action) {
            super(actionDef, action);
        }
    }
}
