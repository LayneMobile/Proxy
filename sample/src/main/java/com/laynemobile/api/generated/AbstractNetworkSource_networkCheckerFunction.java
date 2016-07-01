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

package com.laynemobile.api.generated;

import com.laynemobile.api.NetworkChecker;
import com.laynemobile.api.templates.NetworkSource_networkCheckerFunction;
import com.laynemobile.proxy.NamedMethodHandler;
import com.laynemobile.proxy.annotations.Generate;
import com.laynemobile.proxy.functions.AbstractProxyFunction;
import com.laynemobile.proxy.functions.Func0;
import com.laynemobile.proxy.functions.FunctionHandlers;

@Generate.ProxyFunctionImplementation(NetworkSource_networkCheckerFunction.class)
public abstract class AbstractNetworkSource_networkCheckerFunction
        extends AbstractProxyFunction<Func0<NetworkChecker>> {
    protected static final String NAME = "networkChecker";

    public AbstractNetworkSource_networkCheckerFunction(Func0<NetworkChecker> networkCheckerFunc0) {
        super(networkCheckerFunc0);
    }

    @Override public NamedMethodHandler handler() {
        return new NamedMethodHandler.Builder()
                .setName(NAME)
                .setMethodHandler(FunctionHandlers.from(function()))
                .build();
    }
}
