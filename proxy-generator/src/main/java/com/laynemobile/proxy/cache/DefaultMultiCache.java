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

public class DefaultMultiCache<K1, K2, V, P> implements MultiCache<K1, K2, V, P> {
    private final Cache<K1, ? extends Cache<K2, ? extends V, P>, P> cache;

    private DefaultMultiCache(Cache<K1, ? extends Cache<K2, ? extends V, P>, P> cache) {
        this.cache = cache;
    }

    public static <K1, K2, V, P> MultiCache<K1, K2, V, P> create(
            Cache<K1, ? extends Cache<K2, ? extends V, P>, P> cache) {
        return new DefaultMultiCache<>(cache);
    }

    public static <K1, K2, V, P> MultiCache<K1, K2, V, P> create(
            Cache.Creator<K1, ? extends Cache<K2, ? extends V, P>, P> creator) {
        return new DefaultMultiCache<>(AbstractCache.create(creator));
    }

    public static <K1, K2, V, P> MultiCache<K1, K2, V, P> create(Creator<K1, K2, V, P> creator) {
        return create(new DefaultCache<>(creator));
    }

    @Override public final V getOrCreate(K1 k1, K2 k2, P p) {
        return getOrCreateChild(k1, p)
                .getOrCreate(k2, p);
    }

    @Override public final V get(K1 k1, K2 k2) {
        Cache<K2, ? extends V, P> child = cache.get(k1);
        return child == null ? null : child.get(k2);
    }

    private Cache<K2, ? extends V, P> getOrCreateChild(K1 k1, P p) {
        return cache.getOrCreate(k1, p);
    }

    private static final class DefaultCache<K1, K2, V, P>
            extends AbstractCache<K1, Cache<K2, ? extends V, P>, P> {
        private final MultiCache.Creator<K1, K2, V, P> creator;

        private DefaultCache(MultiCache.Creator<K1, K2, V, P> creator) {
            this.creator = creator;
        }

        @Override protected Cache<K2, ? extends V, P> create(K1 k1, P p) {
            return new ChildCache(k1);
        }

        private final class ChildCache extends AbstractCache<K2, V, P> {
            private final K1 k1;

            private ChildCache(K1 k1) {
                this.k1 = k1;
            }

            @Override protected V create(K2 k2, P p) {
                return creator.create(k1, k2, p);
            }
        }
    }
}
