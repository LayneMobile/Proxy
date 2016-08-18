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

abstract class BaseProxyFunction<F extends FunctionTransform<?>, R> extends ConcreteFunctionDef<R>
        implements ProxyFunction<F, R> {
    private final F function;

    protected BaseProxyFunction(ProxyFunction<F, R> proxyFunction) {
        super(proxyFunction);
        this.function = proxyFunction.function();
    }

    protected BaseProxyFunction(FunctionDef<R> functionDef, F function) {
        super(functionDef);
        this.function = function;
    }

    protected BaseProxyFunction(String name, F function, TypeToken<R> returnType, TypeToken<?>[] paramTypes) {
        super(name, returnType, paramTypes);
        this.function = function;
    }

    @Override public final F function() {
        return function;
    }
}
