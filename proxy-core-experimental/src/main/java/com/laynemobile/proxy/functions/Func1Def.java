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
import com.laynemobile.proxy.functions.transforms.Func1Transform;

public class Func1Def<T, R> extends FunctionDef<Func1Transform<T, R>, R> {
    public Func1Def(String name, TypeToken<R> returnType, TypeToken<T> tType) {
        super(name, returnType, new TypeToken<?>[]{tType});
    }

    @Override public Function<T, R> asFunction(Func1Transform<T, R> transform) {
        return new Function<>(this, transform);
    }

    public static class Function<T, R> extends ProxyFunction<Func1Transform<T, R>, R> {
        protected Function(Func1Def<T, R> functionDef, Func1Transform<T, R> function) {
            super(functionDef, function);
        }
    }
}
