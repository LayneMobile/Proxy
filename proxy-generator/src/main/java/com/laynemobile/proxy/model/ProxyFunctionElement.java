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

package com.laynemobile.proxy.model;

import javax.lang.model.element.ExecutableElement;

import sourcerer.processor.Env;

public class ProxyFunctionElement {
    private final ProxyElement parent;
    private final ExecutableElement element;

    private ProxyFunctionElement(ProxyElement parent, ExecutableElement element) {
        this.parent = parent;
        this.element = element;
    }

    public static ProxyFunctionElement create(ProxyElement parent, ExecutableElement element, Env env) {
        // TODO:
        return new ProxyFunctionElement(parent, element);
    }
}
