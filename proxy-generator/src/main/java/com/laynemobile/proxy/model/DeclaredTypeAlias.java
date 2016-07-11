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
import com.google.common.collect.ImmutableList;
import com.laynemobile.proxy.cache.AliasCache;

import java.util.List;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.WildcardType;

import sourcerer.processor.Env;

public final class DeclaredTypeAlias extends AbstractValueAlias<DeclaredType> {
    private final TypeElementAlias element;
    private final ImmutableList<? extends DeclaredTypeAlias> directSuperTypes;

    private DeclaredTypeAlias(DeclaredType declaredType, TypeElementAlias typeElementAlias,
            List<? extends DeclaredTypeAlias> directSuperTypes, Env env) {
        super(declaredType);
        List<? extends TypeMirror> typeArguments = declaredType.getTypeArguments();
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
        this.element = typeElementAlias;
        this.directSuperTypes = ImmutableList.copyOf(directSuperTypes);
    }

    public static AliasCache<DeclaredType, ? extends DeclaredTypeAlias, TypeMirror> cache() {
        return Cache.INSTANCE;
    }

    public final DeclaredType type() {
        return value();
    }

    public TypeElementAlias element() {
        return element;
    }

    public ImmutableList<? extends DeclaredTypeAlias> directSuperTypes() {
        return directSuperTypes;
    }

    @Override public boolean equals(Object o) {
        return this == o || o instanceof DeclaredTypeAlias && super.equals(o);
    }

    @Override public int hashCode() {
        return super.hashCode();
    }

    @Override public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("type", type())
                .toString();
    }

    public String toDebugString() {
        return MoreObjects.toStringHelper(this)
                .add("type", type())
                .add("element", element())
                .add("directSuperTypes", directSuperTypes())
                .toString();
    }

    private static final class Cache extends AliasCache<DeclaredType, DeclaredTypeAlias, TypeMirror> {
        private static final Cache INSTANCE = new Cache();

        private Cache() {}

        @Override protected DeclaredType cast(TypeMirror typeMirror) throws Exception {
            if (typeMirror.getKind() != TypeKind.DECLARED) {
                return null;
            }
            return (DeclaredType) typeMirror;
        }

        @Override protected DeclaredTypeAlias create(DeclaredType declaredType, Env env) {
            env.log("creating declared type alias: %s", declaredType);
            TypeElement typeElement = (TypeElement) env.types().asElement(declaredType);
            TypeElementAlias typeElementAlias = TypeElementAlias.cache().getOrCreate(typeElement, env);

            ImmutableList.Builder<DeclaredTypeAlias> directSuperTypes = ImmutableList.builder();
            for (TypeMirror typeMirror : env.types().directSupertypes(declaredType)) {
                if (typeMirror.getKind() == TypeKind.DECLARED) {
                    directSuperTypes.add(getOrCreate((DeclaredType) typeMirror, env));
                }
            }

            DeclaredTypeAlias typeAlias
                    = new DeclaredTypeAlias(declaredType, typeElementAlias, directSuperTypes.build(), env);
            env.log("created type: %s\n\n", typeAlias.toDebugString());
            return typeAlias;
        }
    }
}
