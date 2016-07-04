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

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import sourcerer.processor.Env;

public class ProxyElementType extends ProxyElement {
    private final DeclaredType type;

    private ProxyElementType(DeclaredType type, ProxyElement copy) {
        super(copy);
        this.type = type;
    }

    public static ProxyElementType create(TypeMirror type, Env env) {
        if (type.getKind() != TypeKind.DECLARED) {
            return null;
        }

        DeclaredType declaredType = (DeclaredType) type;
        TypeElement interfaceElement = (TypeElement) env.types().asElement(type);
        ProxyElement proxyElement = ProxyElement.create(interfaceElement, env);
        return proxyElement == null ? null : new ProxyElementType(declaredType, proxyElement);
    }

    public DeclaredType type() {
        return type;
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProxyElementType)) return false;
        if (!super.equals(o)) return false;
        ProxyElementType that = (ProxyElementType) o;
        return Objects.equal(type, that.type);
    }

    @Override public int hashCode() {
        return Objects.hashCode(super.hashCode(), type);
    }

    @Override public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("type", type)
                .toString();
    }
}
