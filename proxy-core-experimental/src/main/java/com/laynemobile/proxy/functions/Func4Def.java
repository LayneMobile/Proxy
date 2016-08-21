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
import com.laynemobile.proxy.functions.transforms.Func4Transform;

public class Func4Def<T1, T2, T3, T4, R> extends FunctionDef<Func4Transform<T1, T2, T3, T4, R>, R> {
    public Func4Def(String name, TypeToken<R> returnType, TypeToken<T1> t1, TypeToken<T2> t2, TypeToken<T3> t3,
            TypeToken<T4> t4) {
        super(name, returnType, new TypeToken<?>[]{t1, t2, t3, t4});
    }

    @Override public Function<T1, T2, T3, T4, R> asFunction(Func4Transform<T1, T2, T3, T4, R> transform) {
        return new Function<>(this, transform);
    }

    public static class Function<T1, T2, T3, T4, R> extends ProxyFunction<Func4Transform<T1, T2, T3, T4, R>, R> {
        protected Function(Func4Def<T1, T2, T3, T4, R> functionDef, Func4Transform<T1, T2, T3, T4, R> function) {
            super(functionDef, function);
        }
    }
}
