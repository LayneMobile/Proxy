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

package com.laynemobile.proxy.model.output;

import com.laynemobile.proxy.model.ProxyElement;
import com.squareup.javapoet.TypeSpec;

import java.util.Set;

public class ProxyHandlerBuilderOutputStub extends AbstractTypeElementOutputStub {
    private ProxyElement proxyElement;
    private Set<ProxyFunctionTypeOutputStub> functions;

    private ProxyHandlerBuilderOutputStub(ProxyElement proxyElement) {
        super(proxyElement.packageName() + ".generated", proxyElement.className().simpleName() + "HandlerBuilder");
    }

    public static ProxyHandlerBuilderOutputStub create(ProxyElement proxyElement) {
        return new ProxyHandlerBuilderOutputStub(proxyElement);
    }

    @Override protected TypeSpec build(TypeSpec.Builder classBuilder) {

        return classBuilder.build();
    }
}
