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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.WildcardType;

import sourcerer.processor.Env;

public final class ProxyType {
    private static final Map<DeclaredType, ProxyType> CACHE = new HashMap<>();

    private final DeclaredType type;
    private final ProxyElement element;

    private ProxyType(DeclaredType type, ProxyElement element) {
        this.type = type;
        this.element = element;
    }

    public static ProxyType parse(TypeMirror type, Env env) {
        if (type.getKind() != TypeKind.DECLARED) {
            return null;
        }

        DeclaredType declaredType = (DeclaredType) type;
        synchronized (CACHE) {
            ProxyType proxyType = CACHE.get(type);
            if (proxyType != null) {
                env.log("returning cached proxy type: %s", proxyType);
                return proxyType;
            }
        }

        ProxyType proxyType = parse(declaredType, env);
        if (proxyType != null) {
            env.log("caching proxy type: %s", proxyType);
            synchronized (CACHE) {
                CACHE.put(declaredType, proxyType);
            }
        }
        return proxyType;
    }

    private static ProxyType parse(DeclaredType type, Env env) {
        TypeElement interfaceElement = (TypeElement) env.types().asElement(type);
        ProxyElement proxyElement = ProxyElement.parse(interfaceElement, env);
        if (proxyElement == null) {
            return null;
        }

        List<? extends TypeMirror> typeArguments = type.getTypeArguments();
        env.log("typeArguments: %s", typeArguments);
        for (TypeMirror typeArgument : typeArguments) {
            TypeKind kind = typeArgument.getKind();
            env.log("typeArgument: %s", typeArgument);
            env.log("typeArgument kind: %s", kind);
            if (kind == TypeKind.TYPEVAR) {
                TypeVariable typeVariable = (TypeVariable) typeArgument;
                env.log("typeVariable upperBound: %s", typeVariable.getUpperBound());
                env.log("typeVariable lowerBound: %s", typeVariable.getLowerBound());
            } else if (kind == TypeKind.DECLARED) {
                DeclaredType declaredVar = (DeclaredType) typeArgument;
                env.log("declaredTypeArgument typeArguments: %s", declaredVar.getTypeArguments());
            } else if (kind == TypeKind.WILDCARD) {
                WildcardType wildcardType = (WildcardType) typeArgument;
                env.log("wildcardType extendsBound: %s", wildcardType.getExtendsBound());
                env.log("wildcardType superBound: %s", wildcardType.getSuperBound());
            }
        }
        return new ProxyType(type, proxyElement);
    }

    public DeclaredType type() {
        return type;
    }

    public ProxyElement element() {
        return element;
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProxyType)) return false;
        if (!super.equals(o)) return false;
        ProxyType that = (ProxyType) o;
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
