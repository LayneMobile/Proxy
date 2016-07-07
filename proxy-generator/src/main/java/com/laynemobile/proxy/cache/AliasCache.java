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

import com.laynemobile.proxy.internal.ProxyLog;
import com.laynemobile.proxy.model.Alias;

import sourcerer.processor.Env;

public abstract class AliasCache<K extends SK, V extends Alias, SK> extends ParameterizedCache<K, V, Env> {
    protected AliasCache() {}

    protected abstract K cast(SK sk) throws Exception;

    public final V parse(SK superType, Env env) {
        try {
            K k = cast(superType);
            if (k != null) {
                return getOrCreate(k, env);
            }
        } catch (Exception e) {
            env.log("error %s", ProxyLog.getStackTraceString(e));
        }
        return null;
    }

    @Override protected final void log(Env env, String format, Object... args) {
        env.log(format, args);
    }
}
