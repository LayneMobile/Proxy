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

public class ProxyFunc1<T, R> extends AbstractProxyFunction<Func1<T, R>, R> {
    public ProxyFunc1(FunctionDef<Func1<T, R>, R> functionDef) {
        super(functionDef);
    }

    public ProxyFunc1(String name, Func1<T, R> function, TypeToken<R> returnType, TypeToken<?>[] paramTypes) {
        super(name, function, returnType, paramTypes);
    }

    @Override protected final FuncN<R> toFuncN(Func1<T, R> function) {
        return Functions.fromFunc(function);
    }
}
