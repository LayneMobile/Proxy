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
import com.google.common.base.Objects;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.TypeElement;

import sourcerer.processor.Env;

public abstract class AbstractTypeElementOutput implements TypeElementOutput {
    private final TypeElementOutputStub source;
    private final TypeSpec typeSpec;

    protected AbstractTypeElementOutput(TypeElementOutputStub source, TypeSpec typeSpec) {
        this.source = source;
        this.typeSpec = typeSpec;
    }

    @Override public final TypeElementOutputStub source() {
        return source;
    }

    @Override public final TypeSpec typeSpec() {
        return typeSpec;
    }

    @Override public TypeElement element(Env env) {
        return env.elements().getTypeElement(source.qualifiedName());
    }

    @Override public boolean hasOutput() {
        return false;
    }

    @Override public TypeElementOutputStub outputStub(Env env) {
        if (hasOutput()) {
            throw new IllegalStateException("must return outputStub if hasOutput() returns true");
        }
        return null;
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AbstractTypeElementOutput)) return false;
        AbstractTypeElementOutput that = (AbstractTypeElementOutput) o;
        return Objects.equal(source, that.source);
    }

    @Override public int hashCode() {
        return Objects.hashCode(source);
    }

    @Override public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("source", source)
                .toString();
    }
}