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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

class DefaultFunctionInfo<F extends Function, R> implements FunctionInfo<F, R> {
    private final String name;
    private final F function;
    private final TypeToken<R> returnType;
    private final List<TypeToken<?>> paramTypes;
    private final int paramCount;

    DefaultFunctionInfo(FunctionInfo<F, R> functionInfo) {
        this.name = functionInfo.name();
        this.function = functionInfo.function();
        this.returnType = functionInfo.returnType();
        this.paramTypes = functionInfo.paramTypes();
        this.paramCount = functionInfo.paramCount();
    }

    DefaultFunctionInfo(String name, F function, TypeToken<R> returnType, TypeToken<?>[] paramTypes) {
        List<? extends TypeToken<?>> paramTypesList = Arrays.asList(paramTypes.clone());
        this.name = name;
        this.function = function;
        this.returnType = returnType;
        this.paramTypes = Collections.unmodifiableList(paramTypesList);
        this.paramCount = paramTypes.length;
    }

    static <F extends Function, R> DefaultFunctionInfo<F, R> create(String name, F function, TypeToken<R> returnType,
            TypeToken<?>[] paramTypes) {
        return new DefaultFunctionInfo<>(name, function, returnType, paramTypes);
    }

    @Override public final String name() {
        return name;
    }

    @Override public final F function() {
        return function;
    }

    @Override public final TypeToken<R> returnType() {
        return returnType;
    }

    @Override public final List<TypeToken<?>> paramTypes() {
        return paramTypes;
    }

    @Override public final int paramCount() {
        return paramCount;
    }
}
