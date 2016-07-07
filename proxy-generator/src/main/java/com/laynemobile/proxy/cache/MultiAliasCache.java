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

import com.laynemobile.proxy.model.Alias;

import sourcerer.processor.Env;

public final class MultiAliasCache<K1, K2, V extends Alias> implements MultiCache<K1, K2, V, Env> {
    private final MultiCache<K1, K2, V, Env> cache;

    private MultiAliasCache(Creator<K1, K2, V> creator) {
        this.cache = DefaultMultiCache.create(new DefaultCache<>(creator));
    }

    public static <K1, K2, V extends Alias> MultiAliasCache<K1, K2, V> create(Creator<K1, K2, V> creator) {
        return new MultiAliasCache<>(creator);
    }

    @Override public V get(K1 k1, K2 k2) {
        return cache.get(k1, k2);
    }

    @Override public V getOrCreate(K1 k1, K2 k2, Env env) {
        return cache.getOrCreate(k1, k2, env);
    }

    private static final class DefaultCache<K1, K2, V extends Alias> extends EnvCache<K1, EnvCache<K2, V>> {
        private final MultiAliasCache.Creator<K1, K2, V> creator;

        private DefaultCache(MultiAliasCache.Creator<K1, K2, V> creator) {
            this.creator = creator;
        }

        @Override protected EnvCache<K2, V> create(K1 k1, Env env) {
            return new ChildCache(k1);
        }

        private final class ChildCache extends EnvCache<K2, V> {
            private final K1 k1;

            private ChildCache(K1 k1) {
                this.k1 = k1;
            }

            @Override protected V create(K2 k2, Env env) {
                return creator.create(k1, k2, env);
            }
        }
    }

    public interface Creator<K1, K2, V extends Alias> extends MultiCache.Creator<K1, K2, V, Env> {}
}
