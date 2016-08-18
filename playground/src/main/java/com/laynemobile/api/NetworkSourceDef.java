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

package com.laynemobile.api;

import com.laynemobile.api.functions.NetworkSource_networkChecker;
import com.laynemobile.proxy.TypeDef;
import com.laynemobile.proxy.TypeToken;

public class NetworkSourceDef<T, P extends Params> {
    private final TypeToken<NetworkSource<T, P>> type = new TypeToken<NetworkSource<T, P>>() {};
    private final TypeDef<NetworkSource<T, P>> typeDef = new TypeDef.Builder<>(type)
            .addSuperType(new SourceDef<T, P>().typeDef())
            .addFunction(new NetworkSource_networkChecker.Def())
            .build();

    public TypeDef<NetworkSource<T, P>> typeDef() {
        return typeDef;
    }
}
