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

import java.util.HashMap;
import java.util.Map;

public abstract class AliasCache<K, V, P> {
    private final Map<K, V> cache = new HashMap<>();

    protected AliasCache() {}

    protected abstract V create(K k, P p);

    public final V getOrCreate(K key, P p) {
        V cached = get(key);
        if (cached != null) {
            log(p, "returning cached value: %s", cached);
            return cached;
        }

        log(p, "creating value from key: %s", key);
        V created = create(key, p);
        synchronized (cache) {
            cached = get(key);
            if (cached != null) {
                return cached;
            }
            log(p, "caching value: %s", created);
            cache.put(key, created);
            return created;
        }
    }

    public final V get(K key) {
        synchronized (cache) {
            return cache.get(key);
        }
    }

    protected void log(P p, String format, Object... args) {
        System.out.printf(format, args);
        System.out.println();
    }
}
