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
import com.laynemobile.proxy.cache.EnvCache;
import com.laynemobile.proxy.cache.MultiAliasCache;
import com.laynemobile.proxy.elements.ElementAlias;
import com.laynemobile.proxy.elements.ExecutableElementAlias;
import com.laynemobile.proxy.elements.TypeElementAlias;
import com.laynemobile.proxy.elements.VariableElementAlias;
import com.laynemobile.proxy.types.TypeMirrorAlias;

import javax.lang.model.element.ElementKind;
import javax.lang.model.type.TypeMirror;

import sourcerer.processor.Env;

public final class MethodElement extends AbstractValueAlias<ExecutableElementAlias> {
    private static MultiAliasCache<TypeElementAlias, ExecutableElementAlias, MethodElement> CACHE
            = MultiAliasCache.create(new Creator());

    private final TypeElementAlias typeElement;
    private final TypeMirrorAlias returnType;
    private final ImmutableList<? extends VariableElementAlias> params;
    private final ImmutableList<TypeMirrorAlias> paramTypes;

    private MethodElement(TypeElementAlias typeElement, ExecutableElementAlias element, Env env) {
        super(element);
        ImmutableList.Builder<TypeMirrorAlias> paramTypes = ImmutableList.builder();
        for (VariableElementAlias param : element.getParameters()) {
            env.log("param: %s", param);
            ElementKind paramKind = param.getKind();
            env.log("param kind: %s", paramKind);
            TypeMirror paramType = param.asType();
            env.log("param type: %s", paramType);
            paramTypes.add(param.asType());
        }

        this.typeElement = typeElement;
        this.returnType = element.getReturnType();
        this.params = ImmutableList.copyOf(element.getParameters());
        this.paramTypes = paramTypes.build();
    }

    public static MultiAliasCache<TypeElementAlias, ExecutableElementAlias, ? extends MethodElement> cache() {
        return CACHE;
    }

    public static ImmutableList<MethodElement> parse(TypeElementAlias typeElement, Env env) {
        EnvCache<ExecutableElementAlias, MethodElement> cache = CACHE.getOrCreate(typeElement, env);
        ImmutableList.Builder<MethodElement> elements = ImmutableList.builder();
        for (ElementAlias element : typeElement.getEnclosedElements()) {
            if (element.getKind() != ElementKind.METHOD) {
                continue;
            }
            ExecutableElementAlias methodElement = (ExecutableElementAlias) element;
            env.log(methodElement, "processing method element: %s", methodElement);
            elements.add(cache.getOrCreate(methodElement, env));
        }
        return elements.build();
    }

    public boolean overrides(MethodElement overridden, Env env) {
        return env.elements().overrides(element(), overridden.element(), typeElement());
    }

    public final TypeElementAlias typeElement() {
        return typeElement;
    }

    public final ExecutableElementAlias element() {
        return value();
    }

    public TypeMirrorAlias returnType() {
        return returnType;
    }

    public ImmutableList<? extends VariableElementAlias> params() {
        return params;
    }

    public ImmutableList<TypeMirrorAlias> paramTypes() {
        return paramTypes;
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MethodElement)) return false;
        return super.equals(o);
    }

    @Override public int hashCode() {
        return super.hashCode();
    }

    @Override public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("element", element())
                .toString();
    }

    @Override public String toDebugString() {
        return MoreObjects.toStringHelper(this)
                .add("element", element())
                .add("\nreturnType", returnType)
                .add("\nparams", params)
                .add("\nparamTypes", paramTypes)
                .toString();
    }

    private static final class Creator
            implements MultiAliasCache.ValueCreator<TypeElementAlias, ExecutableElementAlias, MethodElement> {

        @Override public MethodElement create(TypeElementAlias typeElement, ExecutableElementAlias element, Env env) {
            MethodElement methodElement = new MethodElement(typeElement, element, env);
            env.log("created method element: %s\n\n", methodElement.toDebugString());
            return methodElement;
        }
    }
}
