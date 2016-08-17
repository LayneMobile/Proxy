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

abstract class BaseProxyFunction<R, F extends Function> implements ProxyFunction<F> {
    private final String name;
    private final F function;
    private final TypeToken<R> returnType;

    BaseProxyFunction(String name, F function, TypeToken<R> returnType) {
        this.name = name;
        this.function = function;
        this.returnType = returnType;
    }

    @Override public final String name() {
        return name;
    }

    @Override public final F function() {
        return function;
    }

    public final TypeToken<R> returnType() {
        return returnType;
    }
}
