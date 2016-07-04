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
import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.WildcardType;

import sourcerer.processor.Env;

public class ProxyElement {
    private final TypeElement element;
    private final ClassName className;
    private final ImmutableList<TypeVariable> typeVariables;
    private final ImmutableList<ProxyElementType> interfaceTypes;

    protected ProxyElement(ProxyElement copy) {
        this.element = copy.element;
        this.className = copy.className;
        this.typeVariables = copy.typeVariables;
        this.interfaceTypes = copy.interfaceTypes;
    }

    private ProxyElement(TypeElement element, List<TypeVariable> typeVariables, List<ProxyElementType> interfaceTypes) {
        this.element = element;
        this.className = ClassName.get(element);
        this.typeVariables = ImmutableList.copyOf(typeVariables);
        this.interfaceTypes = ImmutableList.copyOf(interfaceTypes);
    }

    public static ProxyElement create(Element element, Env env) {
        // Ensure it is an interface element
        if (element.getKind() != ElementKind.INTERFACE) {
            env.error(element, "Only interfaces can be annotated with @%s",
                    Generate.ProxyBuilder.class.getSimpleName());
            return null; // Exit processing
        }
        return create((TypeElement) element, env);
    }

    private static ProxyElement create(TypeElement element, Env env) {
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

        List<ProxyElementType> interfaceTypes = new ArrayList<>(interfaces.size());
        for (TypeMirror interfaceType : interfaces) {
            ProxyElementType elementType = ProxyElementType.create(interfaceType, env);
            if (elementType != null) {
                env.log("interface type: %s", interfaceType);
                interfaceTypes.add(elementType);
                List<? extends TypeMirror> typeArguments = elementType.type().getTypeArguments();
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
            }
        }
        return new ProxyElement(element, typeVariables, interfaceTypes);
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

    public ImmutableList<ProxyElementType> interfaceTypes() {
        return interfaceTypes;
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
