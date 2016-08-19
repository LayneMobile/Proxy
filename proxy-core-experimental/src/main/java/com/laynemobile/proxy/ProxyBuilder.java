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
import com.laynemobile.proxy.functions.ProxyFunction;
import com.laynemobile.proxy.internal.ProxyLog;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

public class ProxyBuilder<T> implements Builder<T> {
    private final TypeToken<T> type;
    private final SortedSet<ProxyType<? extends T>> handlers;

    public ProxyBuilder(TypeToken<T> type) {
        this.type = type;
        this.handlers = new TreeSet<>();
    }

    public ProxyBuilder(ProxyType<T> parent) {
        this(parent.type());
        handlers.add(parent);
    }

    public final ProxyBuilder<T> add(ProxyType<? extends T> handler) {
        throwIfContains(handler);
        this.handlers.add(handler);
        return this;
    }

    @SafeVarargs public final ProxyBuilder<T> addAll(ProxyType<? extends T>... handlers) {
        for (ProxyType<? extends T> handler : handlers) {
            add(handler);
        }
        return this;
    }

    public final ProxyBuilder<T> addAll(Collection<? extends ProxyType<? extends T>> handlers) {
        for (ProxyType<? extends T> handler : handlers) {
            add(handler);
        }
        return this;
    }

    public final boolean contains(Class<?> type) {
        for (ProxyType<?> module : handlers) {
            if (module.rawTypes().contains(type)) {
                return true;
            }
        }
        return false;
    }

    public final void verifyContains(Class<?> type) {
        if (!contains(type)) {
            String msg = String.format("builder must have type '%s'. You might have forgot a module", type);
            throw new IllegalStateException(msg);
        }
    }

    public final void verifyContains(Collection<? extends Class<?>> types) {
        for (Class<?> type : types) {
            verifyContains(type);
        }
    }

    @Override public final T build() {
        if (handlers.isEmpty()) {
            throw new IllegalStateException("no handlers");
        } else if (!contains(type.getRawType())) {
            String msg = String.format(Locale.US, "must contain '%s' handler", type);
            throw new IllegalStateException(msg);
        }
        return build(type, handlers);
    }

    private boolean contains(TypeToken<?> type) {
        return contains(type.getRawType());
    }

    private void throwIfContains(TypeToken<?> type) {
        if (contains(type)) {
            String msg = String.format("handler type '%s' already defined", type);
            throw new IllegalStateException(msg);
        }
    }

    private void throwIfContains(ProxyType<?> handler) {
        throwIfContains(handler.type());
        for (ProxyType<?> superType : handler.superTypes()) {
            throwIfContains(superType);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T build(TypeToken<T> baseType, SortedSet<ProxyType<? extends T>> proxyTypes) {
        List<Class<?>> classes = new ArrayList<>(proxyTypes.size());
        Map<String, List<ProxyFunction<?, ?>>> handlers = new HashMap<>();
        for (ProxyType<? extends T> proxyType : proxyTypes) {
            addProxyType(proxyType, classes, handlers);
        }

        // Verify we have every method implemented
        verifyFunctionsImplemented(proxyTypes, handlers);

        // Add ProxyObject interface
        ProxyType<ProxyObject> proxyObjectType = buildProxyObjectType(proxyTypes);
        addProxyType(proxyObjectType, classes, handlers);

        ClassLoader cl = baseType.getRawType().getClassLoader();
        Class[] ca = classes.toArray(new Class[classes.size()]);
        return (T) Proxy.newProxyInstance(cl, ca, new ProxyInvocationHandler(handlers));
    }

    private static <T> void addProxyType(ProxyType<? extends T> proxyType, List<Class<?>> classes,
            Map<String, List<ProxyFunction<?, ?>>> handlers) {
        ProxyLog.d("ProxyBuilder", "proxyType: %s", proxyType.type());
        classes.addAll(proxyType.rawTypes());
        for (ProxyFunction<?, ?> function : proxyType.allFunctions()) {
            final String name = function.name();
            ProxyLog.d("ProxyBuilder", "proxyType: %s, function: %s", proxyType.type(), name);
            List<ProxyFunction<?, ?>> current = handlers.get(name);
            if (current == null) {
                current = new ArrayList<>();
                handlers.put(name, current);
            }
            current.add(function);
        }
    }

    private static <T> ProxyType<ProxyObject> buildProxyObjectType(Collection<ProxyType<? extends T>> proxyTypes) {
        final ProxyObject temp = ImmutableProxyObject.builder()
                .setProxyTypes(proxyTypes)
                .build();
        return new ProxyObjectProxyTypeBuilder()
                .setProxyTypes(temp.proxyTypes())
                .setToString(temp.toString())
                .buildProxyType();
    }

    private static <T> void verifyFunctionsImplemented(Collection<ProxyType<? extends T>> proxyTypes,
            Map<String, List<ProxyFunction<?, ?>>> handlers) {
        for (ProxyType<? extends T> proxyType : proxyTypes) {
            TypeDef<? extends T> typeDef = proxyType.definition();
            for (FunctionDef<?> functionDef : typeDef.allFunctions()) {
                String name = functionDef.name();
                List<? extends FunctionDef<?>> implList = handlers.get(name);
                if (implList == null) {
                    throw new IllegalStateException("must implement " + functionDef);
                }
                if (!implList.contains(functionDef)) {
                    throw new IllegalStateException("must implement " + functionDef);
                }
            }
        }
    }
}
