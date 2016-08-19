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

package com.laynemobile.proxy;

import com.laynemobile.proxy.functions.ProxyFunc0;
import com.laynemobile.proxy.functions.transforms.Func0Transform;

import java.util.List;

final class ProxyObject_proxyTypes extends ProxyFunc0<List<ProxyType<?>>> {
    ProxyObject_proxyTypes(Func0Transform<List<ProxyType<?>>> function) {
        super(new Def(), function);
    }

    static final class Def extends ProxyFunc0.Def<List<ProxyType<?>>> {
        public Def() {
            super("proxyTypes", new TypeToken<List<ProxyType<?>>>() {});
        }
    }
}
