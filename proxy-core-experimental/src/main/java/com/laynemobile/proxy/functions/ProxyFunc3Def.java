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
import com.laynemobile.proxy.functions.transforms.ProxyFunc3Transform;

public class ProxyFunc3Def<P, T1, T2, T3, R> extends ProxyFunctionDef<P, ProxyFunc3Transform<P, T1, T2, T3, R>, R> {
    public ProxyFunc3Def(String name, TypeToken<R> returnType, TypeToken<T1> t1, TypeToken<T2> t2, TypeToken<T3> t3) {
        super(name, returnType, new TypeToken<?>[]{t1, t2, t3});
    }

    @Override public Function<P, T1, T2, T3, R> asFunction(ProxyFunc3Transform<P, T1, T2, T3, R> transform) {
        return new Function<>(this, transform);
    }

    public static class Function<P, T1, T2, T3, R> extends ProxyFunction2<P, ProxyFunc3Transform<P, T1, T2, T3, R>, R> {
        protected Function(ProxyFunc3Def<P, T1, T2, T3, R> functionDef,
                ProxyFunc3Transform<P, T1, T2, T3, R> function) {
            super(functionDef, function);
        }
    }
}