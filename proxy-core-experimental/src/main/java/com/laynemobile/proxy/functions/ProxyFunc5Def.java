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
import com.laynemobile.proxy.functions.transforms.ProxyFunc5Transform;

public class ProxyFunc5Def<P, T1, T2, T3, T4, T5, R> extends ProxyFunctionDef<P, ProxyFunc5Transform<P, T1, T2, T3, T4, T5, R>, R> {
    public ProxyFunc5Def(String name, TypeToken<R> returnType, TypeToken<T1> t1, TypeToken<T2> t2, TypeToken<T3> t3,
            TypeToken<T4> t4, TypeToken<T5> t5) {
        super(name, returnType, new TypeToken<?>[]{t1, t2, t3, t4, t5});
    }

    @Override
    public Function<P, T1, T2, T3, T4, T5, R> asFunction(ProxyFunc5Transform<P, T1, T2, T3, T4, T5, R> transform) {
        return new Function<>(this, transform);
    }

    public static class Function<P, T1, T2, T3, T4, T5, R> extends ProxyFunction2<P, ProxyFunc5Transform<P, T1, T2, T3, T4, T5, R>, R> {
        protected Function(ProxyFunc5Def<P, T1, T2, T3, T4, T5, R> functionDef,
                ProxyFunc5Transform<P, T1, T2, T3, T4, T5, R> function) {
            super(functionDef, function);
        }
    }
}