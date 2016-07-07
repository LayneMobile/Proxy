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
import com.laynemobile.proxy.cache.EnvCache;
import com.laynemobile.proxy.cache.MultiAliasCache;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

import sourcerer.processor.Env;

public class MethodElement extends Alias {
    private static MultiAliasCache<TypeElement, ExecutableElement, MethodElement> CACHE
            = MultiAliasCache.create(new Creator());

    private final TypeElement typeElement;
    private final ExecutableElement element;
    private final TypeMirror returnType;
    private final ImmutableList<? extends VariableElement> params;
    private final ImmutableList<TypeMirror> paramTypes;

    protected MethodElement(MethodElement source) {
        this.typeElement = source.typeElement;
        this.element = source.element;
        this.returnType = source.returnType;
        this.params = source.params;
        this.paramTypes = source.paramTypes;
    }

    private MethodElement(TypeElement typeElement, ExecutableElement element, Env env) {
        ImmutableList.Builder<TypeMirror> paramTypes = ImmutableList.builder();
        for (VariableElement param : element.getParameters()) {
            env.log("param: %s", param);
            ElementKind paramKind = param.getKind();
            env.log("param kind: %s", paramKind);
            TypeMirror paramType = param.asType();
            env.log("param type: %s", paramType);
            paramTypes.add(param.asType());
        }

        this.typeElement = typeElement;
        this.element = element;
        this.returnType = element.getReturnType();
        this.params = ImmutableList.copyOf(element.getParameters());
        this.paramTypes = paramTypes.build();
    }

    public static MultiAliasCache<TypeElement, ExecutableElement, ? extends MethodElement> cache() {
        return CACHE;
    }

    public static ImmutableList<MethodElement> parse(TypeElement typeElement, Env env) {
        EnvCache<ExecutableElement, MethodElement> cache = CACHE.getOrCreate(typeElement, env);
        ImmutableList.Builder<MethodElement> elements = ImmutableList.builder();
        for (Element element : typeElement.getEnclosedElements()) {
            if (element.getKind() != ElementKind.METHOD) {
                continue;
            }
            ExecutableElement methodElement = (ExecutableElement) element;
            env.log(methodElement, "processing method element: %s", methodElement);
            elements.add(cache.getOrCreate(methodElement, env));
        }
        return elements.build();
    }

    public boolean overrides(MethodElement overridden, Env env) {
        return env.elements().overrides(element, overridden.element, typeElement());
    }

    public final TypeElement typeElement() {
        return typeElement;
    }

    public final ExecutableElement element() {
        return element;
    }

    public TypeMirror returnType() {
        return returnType;
    }

    public ImmutableList<? extends VariableElement> params() {
        return params;
    }

    public ImmutableList<TypeMirror> paramTypes() {
        return paramTypes;
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MethodElement)) return false;
        MethodElement that = (MethodElement) o;
        return Objects.equal(element, that.element);
    }

    @Override public int hashCode() {
        return Objects.hashCode(element);
    }

    @Override public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("element", element)
                .toString();
    }

    @Override protected String toDebugString() {
        return MoreObjects.toStringHelper(this)
                .add("element", element)
                .add("\nreturnType", returnType)
                .add("\nparams", params)
                .add("\nparamTypes", paramTypes)
                .toString();
    }

    private static final class Creator implements MultiAliasCache.ValueCreator<TypeElement, ExecutableElement, MethodElement> {
        @Override public MethodElement create(TypeElement typeElement, ExecutableElement element, Env env) {
            MethodElement methodElement = new MethodElement(typeElement, element, env);
            env.log("created method element: %s\n\n", methodElement.toDebugString());
            return methodElement;
        }
    }
}
