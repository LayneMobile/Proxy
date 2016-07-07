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

public abstract class AliasSubtypeCache<K extends SK, V extends SV, SK, SV extends Alias> extends AliasCache<K, V, SK> {
    private final AliasCache<K, ? extends SV, SK> superCache;

    protected AliasSubtypeCache(AliasCache<K, ? extends SV, SK> superCache) {
        this.superCache = superCache;
    }

    protected abstract V create(SV sv, Env env);

    @Override protected K cast(SK sk) throws Exception {
        return superCache.cast(sk);
    }

    @Override protected final V create(K k, Env env) {
        return create(superCache.getOrCreate(k, env), env);
    }
}
