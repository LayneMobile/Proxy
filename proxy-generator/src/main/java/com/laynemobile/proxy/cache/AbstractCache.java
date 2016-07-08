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

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

public abstract class AbstractCache<K, V, P> implements Cache<K, V, P> {
    private final Map<K, V> cache;

    protected AbstractCache() {
        this(new HashMap<K, V>());
    }

    protected AbstractCache(Map<K, V> cache) {
        this.cache = cache;
    }

    protected abstract V create(K k, P p);

    public static <K, V, P> AbstractCache<K, V, P> create(final Creator<K, V, P> creator) {
        return new AbstractCache<K, V, P>() {
            @Override protected V create(K k, P p) {
                return creator.create(k, p);
            }
        };
    }

    @Override public final V getOrCreate(K k, P p) {
        V cached;
        if ((cached = get(k)) == null) {
            log(p, "creating value from key: %s", k);
            V created = create(k, p);
            log(p, "created value: %s", created);
            synchronized (cache) {
                if ((cached = get(k)) == null) {
                    cache.put(k, created);
                }
            }
            if (cached == null) {
                log(p, "caching value: %s", created);
                return created;
            }
        }
        log(p, "returning cached value: %s", cached);
        return cached;
    }

    @Override public final V get(K key) {
        synchronized (cache) {
            return cache.get(key);
        }
    }

    @Override public final Collection<V> values() {
        synchronized (cache) {
            return new LinkedHashSet<>(cache.values());
        }
    }

    protected void log(P p, String format, Object... args) {
        System.out.printf(format, args);
        System.out.println();
    }
}
