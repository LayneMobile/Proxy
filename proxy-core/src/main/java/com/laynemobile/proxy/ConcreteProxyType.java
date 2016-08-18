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

import com.laynemobile.proxy.functions.ProxyFunction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import static java.util.Collections.unmodifiableList;

final class ConcreteProxyType<T> extends AbstractTypeDef<T, ProxyType<? super T>, ProxyFunction<?, ?>> implements ProxyType<T> {
    private final TypeDef<T> definition;
    private final List<? extends ProxyFunction<?, ?>> functions;

    ConcreteProxyType(TypeDef<T> definition, Collection<? extends ProxyType<? super T>> superTypes,
            Collection<? extends ProxyFunction<?, ?>> functions) {
        super(definition.type(), superTypes);
        this.definition = definition;
        this.functions = unmodifiableList(new ArrayList<>(functions));
    }

    @Override public TypeDef<T> definition() {
        return definition;
    }

    @Override public Set<Class<?>> rawTypes() {
        SortedSet<? extends ProxyType<? super T>> superTypes = superTypes();
        LinkedHashSet<Class<?>> rawTypes = new LinkedHashSet<>(superTypes.size() + 1);
        rawTypes.add(type().getRawType());
        for (ProxyType<? super T> superType : superTypes) {
            rawTypes.add(superType.type().getRawType());
        }
        return rawTypes;
    }

    @Override public List<? extends ProxyFunction<?, ?>> functions() {
        return functions;
    }

    @Override public Set<? extends ProxyFunction<?, ?>> allFunctions() {
        return (Set<? extends ProxyFunction<?, ?>>) super.allFunctions();
    }

    @Override public ProxyType.Builder<T> newProxyBuilder() {
        return new ProxyType.Builder<>(this);
    }

    @Override public int compareTo(TypeDef<?> o) {
        return definition.compareTo(o);
    }
}
