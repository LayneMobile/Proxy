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

class DefaultFunctionDef<R> implements FunctionDef<R> {
    private final String name;
    private final TypeToken<R> returnType;
    private final List<TypeToken<?>> paramTypes;

    DefaultFunctionDef(FunctionDef<R> functionDef) {
        this.name = functionDef.name();
        this.returnType = functionDef.returnType();
        this.paramTypes = functionDef.paramTypes();
    }

    DefaultFunctionDef(String name, TypeToken<R> returnType, TypeToken<?>[] paramTypes) {
        List<? extends TypeToken<?>> paramTypesList = Arrays.asList(paramTypes.clone());
        this.name = name;
        this.returnType = returnType;
        this.paramTypes = Collections.unmodifiableList(paramTypesList);
    }

    static <R> DefaultFunctionDef<R> create(String name, TypeToken<R> returnType, TypeToken<?>[] paramTypes) {
        return new DefaultFunctionDef<>(name, returnType, paramTypes);
    }

    @Override public final String name() {
        return name;
    }

    @Override public final TypeToken<R> returnType() {
        return returnType;
    }

    @Override public final List<TypeToken<?>> paramTypes() {
        return paramTypes;
    }
}
