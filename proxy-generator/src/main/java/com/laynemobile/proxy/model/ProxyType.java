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

import com.google.common.collect.ImmutableList;
import com.laynemobile.proxy.cache.AliasCache;
import com.laynemobile.proxy.cache.AliasSubtypeCache;

import java.util.List;

import javax.lang.model.element.ElementKind;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

import sourcerer.processor.Env;

public class ProxyType extends DeclaredTypeAlias {
    private final ProxyElement element;
    private final ImmutableList<ProxyType> directSuperTypes;

    protected ProxyType(DeclaredType type, ProxyElement element, List<ProxyType> directSuperTypes) {
        super(type, element, directSuperTypes);
        this.element = element;
        this.directSuperTypes = ImmutableList.copyOf(directSuperTypes);
    }

    public static AliasCache<DeclaredType, ? extends ProxyType, TypeMirror> cache() {
        return Cache.INSTANCE;
    }

    @Override public ProxyElement element() {
        return element;
    }

    @Override public ImmutableList<ProxyType> directSuperTypes() {
        return directSuperTypes;
    }

    private static final class Cache extends AliasSubtypeCache<DeclaredType, ProxyType, TypeMirror, DeclaredTypeAlias> {
        private static final Cache INSTANCE = new Cache();

        private Cache() {
            super(DeclaredTypeAlias.cache());
        }

        @Override protected DeclaredType cast(TypeMirror typeMirror) throws Exception {
            DeclaredType declaredType = super.cast(typeMirror);
            return declaredType.asElement().getKind() == ElementKind.INTERFACE
                    ? declaredType
                    : null;
        }

        @Override protected ProxyType create(DeclaredTypeAlias typeAlias, Env env) {
            ProxyElement proxyElement = ProxyElement.cache().getOrCreate(typeAlias.element().element(), env);
            ImmutableList.Builder<ProxyType> directSuperTypes = ImmutableList.builder();
            for (DeclaredTypeAlias superType : typeAlias.directSuperTypes()) {
                if (superType.element().kind() == ElementKind.INTERFACE) {
                    directSuperTypes.add(getOrCreate(superType.type(), env));
                }
            }
            return new ProxyType(typeAlias.type(), proxyElement, directSuperTypes.build());
        }
    }
}
