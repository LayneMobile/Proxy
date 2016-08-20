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

package com.laynemobile.api.cache;

import java.util.Map;

@GenerateProxySpec
public class CacheSpec<K, V> implements ProxySpec<Cache<K, V>, CacheSpec.SharedState<K, V>, CacheSpec.State<K, V>> {
    @Override public State<K, V> createProxyState(Cache<K, V> proxy, SharedState<K, V> sharedState) {
        return new State<>(proxy, sharedState);
    }

    public interface SharedState<K, V> extends ProxyState.SharedState {
        Map<K, V> cache();
    }

    public static class State<K, V> extends ProxyState<Cache<K, V>, SharedState<K, V>> {
        public State(Cache<K, V> proxy, CacheSpec.SharedState<K, V> sharedState) {
            super(proxy, sharedState);
        }
    }
}
