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
import com.laynemobile.proxy.functions.transforms.ProxyFunc1Transform;

public class ProxyFunc1Def<P, T, R> extends ProxyFunctionDef<P, ProxyFunc1Transform<P, T, R>, R> {
    public ProxyFunc1Def(String name, TypeToken<R> returnType, TypeToken<T> t) {
        super(name, returnType, new TypeToken<?>[]{t});
    }

    @Override public Function<P, T, R> asFunction(ProxyFunc1Transform<P, T, R> transform) {
        return new Function<>(this, transform);
    }

    public static class Function<P, T, R> extends ProxyFunction2<P, ProxyFunc1Transform<P, T, R>, R> {
        protected Function(ProxyFunc1Def<P, T, R> functionDef, ProxyFunc1Transform<P, T, R> function) {
            super(functionDef, function);
        }
    }
}
