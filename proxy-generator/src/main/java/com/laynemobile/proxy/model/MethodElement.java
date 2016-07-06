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

import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

import sourcerer.processor.Env;

public class MethodElement {
    private final Env env;
    private final TypeElement typeElement;
    private final ExecutableElement element;
    private final TypeMirror returnType;
    private final ImmutableList<? extends VariableElement> params;
    private final ImmutableList<TypeMirror> paramTypes;

    protected MethodElement(MethodElement source) {
        this.env = source.env;
        this.typeElement = source.typeElement;
        this.element = source.element;
        this.returnType = source.returnType;
        this.params = source.params;
        this.paramTypes = source.paramTypes;
    }

    private MethodElement(Env env, TypeElement typeElement, ExecutableElement element, List<TypeMirror> paramTypes) {
        this.env = env;
        this.typeElement = typeElement;
        this.element = element;
        this.returnType = element.getReturnType();
        this.params = ImmutableList.copyOf(element.getParameters());
        this.paramTypes = ImmutableList.copyOf(paramTypes);
    }

    public static MethodElement parse(TypeElement typeElement, Element element, Env env) {
        if (element.getKind() != ElementKind.METHOD) {
            return null;
        }
        ExecutableElement methodElement = (ExecutableElement) element;
        env.log(methodElement, "processing method element: %s", methodElement);
        return create(typeElement, methodElement, env);
    }

    private static MethodElement create(TypeElement typeElement, ExecutableElement element, Env env) {
        ImmutableList.Builder<TypeMirror> paramTypes = ImmutableList.builder();
        for (VariableElement param : element.getParameters()) {
            env.log("param: %s", param);
            ElementKind paramKind = param.getKind();
            env.log("param kind: %s", paramKind);
            TypeMirror paramType = param.asType();
            env.log("param type: %s", paramType);
            paramTypes.add(param.asType());
        }
        return new MethodElement(env, typeElement, element, paramTypes.build());
    }

    public Env env() {
        return env;
    }

    public TypeElement typeElement() {
        return typeElement;
    }

    public ExecutableElement element() {
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
        return Objects.equal(typeElement, that.typeElement) &&
                Objects.equal(element, that.element);
    }

    @Override public int hashCode() {
        return Objects.hashCode(typeElement, element);
    }

    @Override public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("typeElement", typeElement)
                .add("element", element)
                .add("returnType", returnType)
                .add("params", params)
                .add("paramTypes", paramTypes)
                .toString();
    }
}
