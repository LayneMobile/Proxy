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

package com.laynemobile.proxy.cache;

import com.google.common.collect.ImmutableList;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractCache<K, V> implements Cache<K, V> {
    private final ThreadLocal<Map<K, V>> calls = new ThreadLocal<>();

    private final Map<K, V> cache;

    protected AbstractCache() {
        this(new HashMap<K, V>());
    }

    protected AbstractCache(Map<K, V> cache) {
        this.cache = cache;
    }

    protected abstract V create(K k);

    // Must return instance of FutureValue<V>
    // i.e. <FV extends V & FutureValue<V>>
    protected V createFutureValue() {
        return null;
    }

    public static <K, V> AbstractCache<K, V> create(final Creator<K, V> creator) {
        return new AbstractCache<K, V>() {
            @Override protected V create(K k) {
                return creator.create(k);
            }
        };
    }

    @Override public V get(K key) {
        final V cached = getIfPresent(key);
        if (cached != null) {
            log("returning cached value: %s", cached);
            return cached;
        }

        Map<K, V> threadCalls = calls.get();
        boolean requiresThreadLocalCleanup = false;
        if (threadCalls == null) {
            threadCalls = new HashMap<>();
            calls.set(threadCalls);
            requiresThreadLocalCleanup = true;
        }

        final V ongoingValue = threadCalls.get(key);
        if (ongoingValue != null) {
            return ongoingValue;
        } else if (threadCalls.containsKey(key)) {
            throw new IllegalStateException(
                    "stack overflow! must return <V extends FutureValue<V>> in createFutureValue() for key: " + key);
        }

        try {
            final V _temp = createFutureValue();
            if (_temp != null && !(_temp instanceof FutureValue)) {
                throw new IllegalStateException("createFutureValue() must return instance of FutureValue<V>");
            }
            threadCalls.put(key, _temp);

            log("creating value from key: %s", key);
            // potential recursive call to get(key) inside create
            final V created = create(key);
            log("created value: %s", created);

            V _return;
            String _log;
            synchronized (cache) {
                final V _cached;
                if ((_cached = getIfPresent(key)) == null) {
                    _log = "caching";
                    cache.put(key, _return = created);
                } else {
                    _log = "returning cached";
                    _return = _cached;
                }
            }

            if (_temp != null) {
                @SuppressWarnings("unchecked")
                final FutureValue<V> futureValue = (FutureValue<V>) _temp;
                futureValue.setDelegate(_return);
            }

            log("%s value: %s", _log, _return);
            return _return;
        } finally {
            threadCalls.remove(key);

            if (requiresThreadLocalCleanup) {
                calls.remove();
            }
        }
    }

    protected final V getIfPresent(K key) {
        synchronized (cache) {
            return cache.get(key);
        }
    }

    @Override public final ImmutableList<V> values() {
        synchronized (cache) {
            return ImmutableList.copyOf(cache.values());
        }
    }

    protected void log(String format, Object... args) {
        System.out.printf(format, args);
        System.out.println();
    }

    public interface FutureValue<D> {
        void setDelegate(D delegate);
    }
}
