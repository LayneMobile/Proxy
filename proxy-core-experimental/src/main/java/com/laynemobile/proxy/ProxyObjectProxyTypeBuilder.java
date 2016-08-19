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

import com.laynemobile.proxy.functions.Func0;
import com.laynemobile.proxy.functions.ProxyObject_proxyTypes;
import com.laynemobile.proxy.functions.ProxyObject_toString;
import com.laynemobile.proxy.functions.transforms.Func0Transform;

import java.util.List;

final class ProxyObjectProxyTypeBuilder {
    private ProxyObject_proxyTypes proxyTypes;

    private ProxyObject_toString toString;

    ProxyObjectProxyTypeBuilder setProxyTypes(ProxyObject_proxyTypes proxyTypes) {
        this.proxyTypes = proxyTypes;
        return this;
    }

    ProxyObjectProxyTypeBuilder setProxyTypes(Func0Transform<List<ProxyType<?>>> proxyTypes) {
        return setProxyTypes(new ProxyObject_proxyTypes(proxyTypes));
    }

    ProxyObjectProxyTypeBuilder setProxyTypes(Func0<? extends List<ProxyType<?>>> proxyTypes) {
        return setProxyTypes(new Func0Transform<>(proxyTypes));
    }

    ProxyObjectProxyTypeBuilder setProxyTypes(List<ProxyType<?>> proxyTypes) {
        return setProxyTypes(new Func0Transform<>(proxyTypes));
    }

    ProxyObjectProxyTypeBuilder setToString(ProxyObject_toString toString) {
        this.toString = toString;
        return this;
    }

    ProxyObjectProxyTypeBuilder setToString(Func0Transform<String> toString) {
        return setToString(new ProxyObject_toString(toString));
    }

    ProxyObjectProxyTypeBuilder setToString(Func0<? extends String> toString) {
        return setToString(new Func0Transform<>(toString));
    }

    ProxyObjectProxyTypeBuilder setToString(String toString) {
        return setToString(new Func0Transform<>(toString));
    }

    ProxyType<ProxyObject> buildProxyType() {
        return new ProxyObjectDef().typeDef().newProxyBuilder()
                .addFunction(proxyTypes)
                .addFunction(toString)
                .build();
    }
}
