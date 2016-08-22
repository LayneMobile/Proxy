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
import com.laynemobile.proxy.functions.transforms.ProxyAction2Transform;

public class ProxyAction2Def<P, T1, T2> extends ProxyActionDef<P, ProxyAction2Transform<P, T1, T2>> {
    public ProxyAction2Def(String name, TypeToken<T1> t1, TypeToken<T2> t2) {
        super(name, new TypeToken<?>[]{t1, t2});
    }

    @Override public Action<P, T1, T2> asFunction(ProxyAction2Transform<P, T1, T2> transform) {
        return new Action<>(this, transform);
    }

    public static class Action<P, T1, T2> extends ProxyAction2<P, ProxyAction2Transform<P, T1, T2>> {
        protected Action(ProxyAction2Def<P, T1, T2> actionDef, ProxyAction2Transform<P, T1, T2> action) {
            super(actionDef, action);
        }
    }
}
