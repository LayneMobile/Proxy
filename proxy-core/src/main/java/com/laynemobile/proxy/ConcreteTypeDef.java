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

package com.laynemobile.proxy;

import com.google.common.base.Objects;
import com.laynemobile.proxy.functions.FunctionDef;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.unmodifiableList;

class ConcreteTypeDef<T> implements TypeDef<T> {
    private final TypeToken<T> type;
    private final List<? extends TypeDef<? super T>> superTypes;
    private final List<? extends FunctionDef<?>> functions;

    ConcreteTypeDef(TypeToken<T> type, List<? extends TypeDef<? super T>> superTypes,
            List<? extends FunctionDef<?>> functions) {
        this.type = type;
        this.superTypes = unmodifiableList(new ArrayList<>(superTypes));
        this.functions = unmodifiableList(new ArrayList<>(functions));
    }

    ConcreteTypeDef(TypeDef<T> typeDef) {
        this(typeDef.type(), typeDef.superTypes(), typeDef.functions());
    }

    @Override public final TypeToken<T> type() {
        return type;
    }

    @Override public final List<? extends TypeDef<? super T>> superTypes() {
        return superTypes;
    }

    @Override public final List<? extends FunctionDef<?>> functions() {
        return functions;
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConcreteTypeDef)) return false;
        ConcreteTypeDef<?> that = (ConcreteTypeDef<?>) o;
        return Objects.equal(type, that.type) &&
                Objects.equal(superTypes, that.superTypes) &&
                Objects.equal(functions, that.functions);
    }

    @Override public int hashCode() {
        return Objects.hashCode(type, superTypes, functions);
    }
}
