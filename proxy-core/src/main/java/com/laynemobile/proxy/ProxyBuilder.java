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

import com.laynemobile.proxy.internal.ProxyLog;
import com.laynemobile.proxy.internal.Util;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class ProxyBuilder<T> implements Builder<T> {
    private final TypeToken<T> type;
    private final List<ProxyHandler<? extends T>> handlers;

    public ProxyBuilder(ProxyHandler<T> parent) {
        List<ProxyHandler<? extends T>> handlers = new ArrayList<>();
        handlers.add(parent);
        this.type = parent.type();
        this.handlers = handlers;
    }

    public final ProxyBuilder<T> add(ProxyHandler<? extends T> handler) {
        if (contains(handler.type)) {
            String msg = String.format("handler type '%s' already defined", handler.type);
            throw new IllegalStateException(msg);
        }
        this.handlers.add(handler);
        return this;
    }

    @SafeVarargs public final ProxyBuilder<T> addAll(ProxyHandler<? extends T>... handlers) {
        for (ProxyHandler<? extends T> handler : handlers) {
            add(handler);
        }
        return this;
    }

    public final ProxyBuilder<T> addAll(List<? extends ProxyHandler<? extends T>> handlers) {
        for (ProxyHandler<? extends T> handler : handlers) {
            add(handler);
        }
        return this;
    }

    public final boolean contains(Class<?> type) {
        for (ProxyHandler<?> module : handlers) {
            if (module.type.getRawType().equals(type)) {
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
            String msg = String.format(Locale.US, "must contain '%s' module", type);
            throw new IllegalStateException(msg);
        }
        return create(type, handlers);
    }

    private boolean contains(TypeToken<?> type) {
        for (ProxyHandler<?> module : handlers) {
            if (module.type.equals(type)) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private static <T> T create(TypeToken<T> baseType, Collection<ProxyHandler<? extends T>> extensions) {
        List<Class<?>> classes = new ArrayList<>(extensions.size());
        Map<String, List<MethodHandler>> handlers = new HashMap<>();
        for (ProxyHandler<? extends T> extension : extensions) {
            classes.add(extension.type.getRawType());
            for (Map.Entry<String, List<MethodHandler>> entry : extension.handlers.entrySet()) {
                final String name = entry.getKey();
                List<MethodHandler> current = handlers.get(name);
                if (current == null) {
                    current = new ArrayList<>();
                    handlers.put(name, current);
                }
                current.addAll(entry.getValue());
            }
        }
        ClassLoader cl = baseType.getRawType().getClassLoader();
        Class[] ca = classes.toArray(new Class[classes.size()]);
        return (T) Proxy.newProxyInstance(cl, ca, new InvokeHandler(handlers));
    }

    private static class InvokeHandler implements InvocationHandler {
        private static final String TAG = InvokeHandler.class.getSimpleName();

        private final Map<String, List<MethodHandler>> handlers;

        private InvokeHandler(Map<String, List<MethodHandler>> handlers) {
            this.handlers = Collections.unmodifiableMap(handlers);
        }

        @Override public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            ProxyLog.d(TAG, "calling method: %s", method);
            MethodResult result = new MethodResult();
            List<MethodHandler> handlers = this.handlers.get(method.getName());
            for (MethodHandler handler : Util.nullSafe(handlers)) {
                if (handler.handle(proxy, method, args, result)) {
                    Object r = result.get();
                    ProxyLog.d(TAG, "handled method: %s, result: %s", method, r);
                    return r;
                }
            }
            ProxyLog.w(TAG, "could not find handler for method: %s", method);
            return null;
        }
    }
}
