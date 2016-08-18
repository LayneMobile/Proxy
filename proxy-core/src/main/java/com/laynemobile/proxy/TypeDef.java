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

import com.laynemobile.proxy.functions.FunctionDef;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

public interface TypeDef<T> extends Comparable<TypeDef<?>> {
    TypeToken<T> type();

    SortedSet<? extends TypeDef<? super T>> superTypes();

    List<? extends FunctionDef<?>> functions();

    Set<? extends FunctionDef<?>> allFunctions();

    ProxyType.Builder<T> newProxyBuilder();

    final class Builder<T> implements com.laynemobile.proxy.Builder<TypeDef<T>> {
        private final TypeToken<T> type;
        private final Set<TypeDef<? super T>> superTypes = new HashSet<>();
        private final LinkedHashSet<FunctionDef<?>> functions = new LinkedHashSet<>();

        public Builder(TypeToken<T> type) {
            this.type = type;
        }

        public Builder<T> addSuperType(TypeDef<? super T> superType) {
            if (superType instanceof ProxyType) {
                @SuppressWarnings("unchecked")
                ProxyType<? super T> proxyType = (ProxyType<? super T>) superType;
                return addSuperType(proxyType);
            }
            superTypes.add(superType);
            return this;
        }

        public Builder<T> addSuperType(ProxyType<? super T> superType) {
            return addSuperType(superType.definition());
        }

        public Builder<T> addFunction(FunctionDef<?> function) {
            functions.add(function);
            return this;
        }

        @Override public TypeDef<T> build() {
            return new ConcreteTypeDef<>(type, superTypes, functions);
        }
    }
}
