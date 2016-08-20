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

import com.laynemobile.api.functions.SimpleSource_call__P_Subscriber;
import com.laynemobile.proxy.AbstractProxyDef;
import com.laynemobile.proxy.TypeDef;
import com.laynemobile.proxy.TypeToken;

public class SimpleSourceDef<T> extends AbstractProxyDef<SimpleSource<T>> {
    private final TypeToken<SimpleSource<T>> type = new TypeToken<SimpleSource<T>>() {};
    private final TypeDef<SimpleSource<T>> typeDef = new TypeDef.Builder<>(type)
            .addSuperType(new SourceDef<T, NoParams>().typeDef())
            .addFunction(new SimpleSource_call__P_Subscriber.Def<>())
            .build();

    @Override public TypeDef<SimpleSource<T>> typeDef() {
        return typeDef;
    }
}
