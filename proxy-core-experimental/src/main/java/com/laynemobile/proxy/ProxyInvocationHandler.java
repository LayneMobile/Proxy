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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

final class ProxyInvocationHandler<T> extends AbstractInvocationHandler {
    private static final String TAG = ProxyInvocationHandler.class.getSimpleName();

    private final TypeToken<T> type;
    private final ConcurrentHashMap<String, MethodHandler> handlers;

    ProxyInvocationHandler(TypeToken<T> type, Map<String, ? extends List<? extends NamedMethodHandler>> _handlers) {
        // create additional handlers for ProxyObject<T>
        NamedMethodHandler newProxyBuilder = new ProxyObject_newProxyBuilder<>();
        boolean newProxyBuilderAdded = false;
        NamedMethodHandler castToType = new ProxyObject_castToType<>();
        boolean castToTypeAdded = false;

        ConcurrentHashMap<String, MethodHandler> handlers = new ConcurrentHashMap<>(_handlers.size(), 0.75f, 1);
        for (Map.Entry<String, ? extends List<? extends NamedMethodHandler>> entry : _handlers.entrySet()) {
            if (!newProxyBuilderAdded && (newProxyBuilderAdded = add(newProxyBuilder, entry, handlers))) {
                continue;
            } else if (!castToTypeAdded && (castToTypeAdded = add(castToType, entry, handlers))) {
                continue;
            }
            handlers.put(entry.getKey(), MethodHandlers.create(entry.getValue()));
        }
        if (!newProxyBuilderAdded) {
            handlers.put(newProxyBuilder.name(), newProxyBuilder);
        }
        if (!castToTypeAdded) {
            handlers.put(castToType.name(), castToType);
        }
        this.type = type;
        this.handlers = handlers;
    }

    private boolean add(NamedMethodHandler handler,
            Map.Entry<String, ? extends List<? extends NamedMethodHandler>> entry,
            ConcurrentHashMap<String, MethodHandler> handlers) {
        String name = entry.getKey();
        List<? extends NamedMethodHandler> list = entry.getValue();
        if (name.equals(handler.name())) {
            List<NamedMethodHandler> newList = new ArrayList<>(list);
            newList.add(handler);
            list = newList;
            handlers.put(name, MethodHandlers.create(list));
        }
        return false;
    }

    @Override
    public boolean handle(Object proxy, Method method, Object[] args, MethodResult result) throws Throwable {
        return get(method)
                .handle(proxy, method, args, result);
    }

    private MethodHandler get(Method method) {
        String name = method.getName();
        MethodHandler handler = handlers.get(name);
        if (handler == null) {
            synchronized (handlers) {
                handlers.put(name, handler = MethodHandler.EMPTY);
            }
        }
        return handler;
    }

    private static final class ProxyObject_newProxyBuilder<T> implements NamedMethodHandler {
        private static final String METHOD_NAME = "newProxyBuilder";

        private final TypeToken<ProxyBuilder<T>> returnType = new TypeToken<ProxyBuilder<T>>() {};

        @Override public String name() {
            return METHOD_NAME;
        }

        @Override
        public boolean handle(Object proxy, Method method, Object[] args, MethodResult result) throws Throwable {
            Class<?>[] paramTypes = method.getParameterTypes();
            if (paramTypes.length != 0) {
                return false;
            }

            Class<?> handlerReturnType = this.returnType.getRawType();
            Class<?> returnType = method.getReturnType();
            if (!handlerReturnType.isAssignableFrom(returnType)) {
                ProxyLog.w(TAG, "return type '%s' not instance of handler return type '%s'", returnType,
                        handlerReturnType);
                return false;
            }

            ProxyObject<T> proxyObject = (ProxyObject<T>) proxy;
            ProxyBuilder<T> proxyBuilder = new ProxyBuilder<>(proxyObject);
            result.set(proxyBuilder);
            return true;
        }
    }

    private static final class ProxyObject_castToType<T> implements NamedMethodHandler {
        private static final String METHOD_NAME = "castToType";

        private final TypeToken<T> returnType = new TypeToken<T>() {};

        @Override public String name() {
            return METHOD_NAME;
        }

        @Override
        public boolean handle(Object proxy, Method method, Object[] args, MethodResult result) throws Throwable {
            Class<?>[] paramTypes = method.getParameterTypes();
            if (paramTypes.length != 0) {
                return false;
            }
            result.set(proxy);
            return true;
        }
    }

    // TODO: better implementation
    private static final class MethodHandlers implements MethodHandler {
        private final List<? extends NamedMethodHandler> handlers;
        private final ConcurrentHashMap<Method, NamedMethodHandler> cache;

        private MethodHandlers(List<? extends NamedMethodHandler> handlers) {
            this.handlers = handlers;
            this.cache = new ConcurrentHashMap<>(handlers.size(), 0.75f, 1);
        }

        private static MethodHandler create(List<? extends NamedMethodHandler> methodHandlers) {
            if (methodHandlers == null || methodHandlers.size() == 0) {
                return MethodHandler.EMPTY;
            } else if (methodHandlers.size() == 1) {
                return methodHandlers.get(0);
            }
            return new MethodHandlers(methodHandlers);
        }

        @Override
        public boolean handle(Object proxy, Method method, Object[] args, MethodResult result) throws Throwable {
            NamedMethodHandler cached = cache.get(method);
            if (cached != null) {
                return cached.handle(proxy, method, args, result);
            }
            for (NamedMethodHandler handler : handlers) {
                if (handler.handle(proxy, method, args, result)) {
                    synchronized (cache) {
                        cache.put(method, handler);
                    }
                    return true;
                }
            }
            return false;
        }
    }
}
