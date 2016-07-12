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

import com.google.common.base.MoreObjects;

import javax.lang.model.element.TypeElement;

import sourcerer.processor.Env;

public abstract class AbstractGeneratedTypeElement implements GeneratedTypeElement {
    private final GeneratedTypeElementStub stub;
    private final TypeElementAlias element;

    protected AbstractGeneratedTypeElement(GeneratedTypeElementStub stub, Env env) {
        TypeElement typeElement = env.elements().getTypeElement(stub.qualifiedName());
        // TODO: how do we know if typeElement is source yet?
        this.stub = stub;
        this.element = TypeElementAlias.cache().getOrCreate(typeElement, env);
    }

    @Override public GeneratedTypeElementStub output(Env env) {
        if (hasOutput()) {
            throw new IllegalStateException("must return output if hasOutput() returns true");
        }
        return null;
    }

    @Override public final GeneratedTypeElementStub input() {
        return stub;
    }

    @Override public final TypeElementAlias value() {
        return element;
    }

    @Override public String toDebugString() {
        return MoreObjects.toStringHelper(this)
                .add("input", stub)
                .add("element", element)
                .toString();
    }
}
