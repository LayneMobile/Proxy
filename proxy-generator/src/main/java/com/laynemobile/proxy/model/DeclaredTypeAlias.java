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
import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
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

public final class DeclaredTypeAlias {
    private static final Map<DeclaredType, DeclaredTypeAlias> CACHE = new HashMap<>();

    private final DeclaredType type;
    private final TypeElementAlias element;
    private final ImmutableList<DeclaredTypeAlias> directSuperTypes;

    private DeclaredTypeAlias(DeclaredType type, TypeElementAlias element, List<DeclaredTypeAlias> directSuperTypes) {
        this.type = type;
        this.element = element;
        this.directSuperTypes = ImmutableList.copyOf(directSuperTypes);
    }

    public static DeclaredTypeAlias parse(TypeMirror type, Env env) {
        if (type.getKind() != TypeKind.DECLARED) {
            return null;
        }
        return get((DeclaredType) type, env);
    }

    static DeclaredTypeAlias get(DeclaredType declaredType, Env env) {
        synchronized (CACHE) {
            DeclaredTypeAlias typeAlias = CACHE.get(declaredType);
            if (typeAlias != null) {
                env.log("returning cached declared type alias: %s", typeAlias);
                return typeAlias;
            }
        }
        env.log("creating declared type alias: %s", declaredType);
        DeclaredTypeAlias typeAlias = parse(declaredType, env);
        if (typeAlias != null) {
            env.log("caching declared type alias: %s", typeAlias);
            synchronized (CACHE) {
                CACHE.put(declaredType, typeAlias);
            }
        }
        return typeAlias;
    }

    private static DeclaredTypeAlias parse(DeclaredType type, Env env) {
        TypeElement typeElement = (TypeElement) env.types().asElement(type);
        TypeElementAlias typeElementAlias = TypeElementAlias.get(typeElement, env);

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

        List<? extends TypeMirror> _directSupertypes = env.types().directSupertypes(type);
        List<DeclaredTypeAlias> directSuperTypes = new ArrayList<>(_directSupertypes.size());
        for (TypeMirror typeMirror : _directSupertypes) {
            if (typeMirror.getKind() == TypeKind.DECLARED) {
                directSuperTypes.add(get((DeclaredType) typeMirror, env));
            }
        }

        return new DeclaredTypeAlias(type, typeElementAlias, directSuperTypes);
    }

    public DeclaredType type() {
        return type;
    }

    public TypeElementAlias element() {
        return element;
    }

    public ImmutableList<DeclaredTypeAlias> directSuperTypes() {
        return directSuperTypes;
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DeclaredTypeAlias)) return false;
        if (!super.equals(o)) return false;
        DeclaredTypeAlias that = (DeclaredTypeAlias) o;
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
