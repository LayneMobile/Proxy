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

package com.laynemobile.api.generated2;

import com.laynemobile.api.NetworkChecker;
import com.laynemobile.api.NetworkSource;
import com.laynemobile.api.Params;
import com.laynemobile.api.templates.NetworkSource_networkCheckerFunction;
import com.laynemobile.proxy.Builder;
import com.laynemobile.proxy.NamedMethodHandler;
import com.laynemobile.proxy.ProxyHandler;
import com.laynemobile.proxy.TypeToken;
import com.laynemobile.proxy.functions.Func0;

// Generated builder (2nd pass)
// @Requires(Source.class)
public class NetworkSourceHandlerBuilder<T, P extends Params>
        implements Builder<ProxyHandler<NetworkSource<T, P>>> {
    private NetworkSource_networkCheckerFunction networkChecker;

    public NetworkSourceHandlerBuilder<T, P> networkChecker(Func0<NetworkChecker> networkChecker) {
        this.networkChecker = new NetworkSource_networkCheckerFunction(networkChecker);
        return this;
    }

    public NetworkSourceHandlerBuilder<T, P> networkChecker(NetworkChecker networkChecker) {
        this.networkChecker = new NetworkSource_networkCheckerFunction(networkChecker);
        return this;
    }

    public NetworkSourceHandlerBuilder<T, P> networkChecker() {
        this.networkChecker = new NetworkSource_networkCheckerFunction();
        return this;
    }

    @Override public ProxyHandler<NetworkSource<T, P>> build() {
        final NetworkSource_networkCheckerFunction networkChecker = this.networkChecker;
        if (networkChecker == null) {
            throw new IllegalStateException("networkChecker function must be set");
        }
        final NamedMethodHandler handler = networkChecker.handler();
        if (handler == null) {
            throw new IllegalStateException("networkChecker function handler must not be null");
        }
        return ProxyHandler.builder(new TypeToken<NetworkSource<T, P>>() {})
                .handle(handler)
                .build();
    }
}
