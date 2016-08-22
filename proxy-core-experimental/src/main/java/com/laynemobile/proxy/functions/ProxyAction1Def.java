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
import com.laynemobile.proxy.functions.transforms.ProxyAction1Transform;

public class ProxyAction1Def<P, T> extends ProxyActionDef<P, ProxyAction1Transform<P, T>> {
    public ProxyAction1Def(String name, TypeToken<T> t) {
        super(name, new TypeToken<?>[]{t});
    }

    @Override public Action<P, T> asFunction(ProxyAction1Transform<P, T> transform) {
        return new Action<>(this, transform);
    }

    public static class Action<P, T> extends ProxyAction2<P, ProxyAction1Transform<P, T>> {
        protected Action(ProxyAction1Def<P, T> actionDef, ProxyAction1Transform<P, T> action) {
            super(actionDef, action);
        }
    }
}