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
import java.util.Collection;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableSortedSet;

class ConcreteTypeDef<T> implements TypeDef<T> {
    private final TypeToken<T> type;
    private final SortedSet<? extends TypeDef<? super T>> superTypes;
    private final List<? extends FunctionDef<?>> functions;

    ConcreteTypeDef(TypeToken<T> type, Collection<? extends TypeDef<? super T>> superTypes,
            Collection<? extends FunctionDef<?>> functions) {
        this.type = type;
        this.superTypes = unmodifiableSortedSet(new TreeSet<>(superTypes));
        this.functions = unmodifiableList(new ArrayList<>(functions));
    }

    ConcreteTypeDef(TypeDef<T> typeDef) {
        this(typeDef.type(), typeDef.superTypes(), typeDef.functions());
    }

    @Override public final TypeToken<T> type() {
        return type;
    }

    @Override public final SortedSet<? extends TypeDef<? super T>> superTypes() {
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

    @Override public int compareTo(TypeDef<?> o) {
        if (equals(o)) {
            return 0;
        } else if (dependsOn(o, this)) {
            return -1;
        } else if (dependsOn(this, o)) {
            return 1;
        }
        return name(this).compareTo(name(o));
    }

    private static boolean dependsOn(TypeDef<?> type, TypeDef<?> test) {
        for (TypeDef<?> superType : type.superTypes()) {
            if (superType.equals(test)) {
                return true;
            } else if (dependsOn(superType, test)) {
                return true;
            }
        }
        return false;
    }

    private static String name(TypeDef<?> o) {
        return o.type().getRawType().getSimpleName();
    }
}
