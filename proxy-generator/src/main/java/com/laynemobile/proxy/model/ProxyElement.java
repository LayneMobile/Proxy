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
import com.laynemobile.proxy.annotations.Generate;
import com.squareup.javapoet.ClassName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;

import sourcerer.processor.Env;

public final class ProxyElement {
    private static final Map<TypeElement, ProxyElement> CACHE = new HashMap<>();

    private final TypeElement element;
    private final ClassName className;
    private final ImmutableList<TypeVariable> typeVariables;
    private final ImmutableList<ProxyType> interfaceTypes;
    private final ImmutableList<FunctionElement> functions;

    private ProxyElement(TypeElement element, List<TypeVariable> typeVariables, List<ProxyType> interfaceTypes,
            List<FunctionElement> functions) {
        this.element = element;
        this.className = ClassName.get(element);
        this.typeVariables = ImmutableList.copyOf(typeVariables);
        this.interfaceTypes = ImmutableList.copyOf(interfaceTypes);
        this.functions = ImmutableList.copyOf(functions);
    }

    public static ProxyElement parse(Element element, Env env) {
        // Ensure it is an interface element
        if (element.getKind() != ElementKind.INTERFACE) {
            env.error(element, "Only interfaces can be annotated with @%s",
                    Generate.ProxyBuilder.class.getSimpleName());
            return null; // Exit processing
        }
        TypeElement typeElement = (TypeElement) element;
        synchronized (CACHE) {
            ProxyElement proxyElement = CACHE.get(typeElement);
            if (proxyElement != null) {
                env.log("returning cached proxy element: %s", proxyElement);
                return proxyElement;
            }
        }
        ProxyElement proxyElement = parse(typeElement, env);
        env.log("caching proxy element: %s", proxyElement);
        synchronized (CACHE) {
            CACHE.put(typeElement, proxyElement);
        }
        return proxyElement;
    }

    private static ProxyElement parse(TypeElement element, Env env) {
        List<? extends TypeMirror> interfaces = element.getInterfaces();
        List<? extends TypeParameterElement> typeParameters = element.getTypeParameters();

        List<TypeVariable> typeVariables = new ArrayList<>(typeParameters.size());
        env.log("typeParameters: %s", typeParameters);
        for (TypeParameterElement typeParameter : typeParameters) {
            env.log("typeParameter: %s", typeParameter);
            env.log("typeParameter bounds: %s", typeParameter.getBounds());
            ElementKind paramKind = typeParameter.getKind();
            env.log("typeParameter kind: %s", paramKind);
            TypeMirror paramType = typeParameter.asType();
            env.log("typeParameter type: %s", paramType);
            if (paramType.getKind() == TypeKind.TYPEVAR) {
                typeVariables.add((TypeVariable) paramType);
            }
        }

        List<ProxyType> interfaceTypes = new ArrayList<>(interfaces.size());
        for (TypeMirror interfaceType : interfaces) {
            ProxyType elementType = ProxyType.parse(interfaceType, env);
            if (elementType != null) {
                env.log("interface type: %s", interfaceType);
                interfaceTypes.add(elementType);
            }
        }

        List<? extends Element> enclosedElements = element.getEnclosedElements();
        List<FunctionElement> functions = new ArrayList<>(enclosedElements.size());
        for (Element enclosed : enclosedElements) {
            FunctionElement functionElement = FunctionElement.parse(enclosed, env);
            if (functionElement == null) {
                continue;
            }
            functions.add(functionElement);
        }
        return new ProxyElement(element, typeVariables, interfaceTypes, functions);
    }

    public TypeElement element() {
        return element;
    }

    public ClassName className() {
        return className;
    }

    public String packageName() {
        return className.packageName();
    }

    public ImmutableList<TypeVariable> typeVariables() {
        return typeVariables;
    }

    public ImmutableList<ProxyType> interfaceTypes() {
        return interfaceTypes;
    }

    public ImmutableList<FunctionElement> functions() {
        return functions;
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProxyElement)) return false;
        ProxyElement that = (ProxyElement) o;
        return Objects.equal(element, that.element);
    }

    @Override public int hashCode() {
        return Objects.hashCode(element);
    }

    @Override public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("className", className)
                .add("element", element)
                .add("typeVariables", typeVariables)
                .add("interfaceTypes", interfaceTypes)
                .toString();
    }
}
