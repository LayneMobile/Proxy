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

import com.google.common.base.Objects;
import com.laynemobile.proxy.TypeToken;

import java.util.Arrays;
import java.util.List;

import static java.util.Collections.unmodifiableList;

public class ConcreteFunctionDef<R> implements FunctionDef<R> {
    private final String name;
    private final TypeToken<R> returnType;
    private final List<TypeToken<?>> paramTypes;

    protected ConcreteFunctionDef(FunctionDef<R> functionDef) {
        this.name = functionDef.name();
        this.returnType = functionDef.returnType();
        this.paramTypes = functionDef.paramTypes();
    }

    protected ConcreteFunctionDef(String name, TypeToken<R> returnType, TypeToken<?>[] paramTypes) {
        List<? extends TypeToken<?>> paramTypesList = Arrays.asList(paramTypes.clone());
        this.name = name;
        this.returnType = returnType;
        this.paramTypes = unmodifiableList(paramTypesList);
    }

    static <R> ConcreteFunctionDef<R> create(String name, TypeToken<R> returnType, TypeToken<?>[] paramTypes) {
        return new ConcreteFunctionDef<>(name, returnType, paramTypes);
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

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConcreteFunctionDef)) return false;
        ConcreteFunctionDef<?> that = (ConcreteFunctionDef<?>) o;
        return Objects.equal(name, that.name) &&
                Objects.equal(returnType, that.returnType) &&
                Objects.equal(paramTypes, that.paramTypes);
    }

    @Override public int hashCode() {
        return Objects.hashCode(name, returnType, paramTypes);
    }
}
