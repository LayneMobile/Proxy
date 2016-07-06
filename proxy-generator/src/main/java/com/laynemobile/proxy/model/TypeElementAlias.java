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

public class TypeElementAlias {
    private static final Map<TypeElement, TypeElementAlias> CACHE = new HashMap<>();
    private static volatile TypeElementAlias OBJECT_ELEMENT;

    private final Env env;
    private final TypeElement element;
    private final ElementKind kind;
    private final ClassName className;
    private final DeclaredTypeAlias superClass;
    private final ImmutableList<TypeVariable> typeVariables;
    private final ImmutableList<DeclaredTypeAlias> interfaceTypes;
    private final ImmutableList<FunctionElement> functions;

    protected TypeElementAlias(TypeElementAlias source) {
        this.env = source.env;
        this.element = source.element;
        this.kind = source.kind;
        this.className = source.className;
        this.superClass = source.superClass;
        this.typeVariables = source.typeVariables;
        this.interfaceTypes = source.interfaceTypes;
        this.functions = source.functions;
    }

    /* java.lang.Object element constructor. */
    private TypeElementAlias(Env env) {
        this.env = env;
        this.element = env.elements().getTypeElement("java.lang.Object");
        this.kind = ElementKind.CLASS;
        this.className = ClassName.get(element);
        this.superClass = null;
        this.typeVariables = ImmutableList.of();
        this.interfaceTypes = ImmutableList.of();
        this.functions = ImmutableList.of();
    }

    private TypeElementAlias(Env env, TypeElement element, DeclaredTypeAlias superClass,
            List<TypeVariable> typeVariables, List<DeclaredTypeAlias> interfaceTypes, List<FunctionElement> functions) {
        this.env = env;
        this.element = element;
        this.kind = element.getKind();
        this.className = ClassName.get(element);
        this.superClass = superClass;
        this.typeVariables = ImmutableList.copyOf(typeVariables);
        this.interfaceTypes = ImmutableList.copyOf(interfaceTypes);
        this.functions = ImmutableList.copyOf(functions);
    }

    public static TypeElementAlias parse(Element element, Env env) {
        if (element.getKind() == ElementKind.CLASS || element.getKind() == ElementKind.INTERFACE) {
            return get((TypeElement) element, env);
        }
        return null;
    }

    static TypeElementAlias get(TypeElement typeElement, Env env) {
        if (typeElement.getKind() == ElementKind.CLASS) {
            // Special parsing for java.lang.Object class
            TypeElementAlias objectElement = objectElement(env);
            if (objectElement.element.equals(typeElement)) {
                return objectElement;
            }
        }
        TypeElementAlias cached = cached(typeElement);
        if (cached != null) {
            env.log("returning cached type element alias: %s", cached.className);
            return cached;
        }
        env.log("creating type element alias: %s", typeElement);
        TypeElementAlias created = create(typeElement, env);
        synchronized (CACHE) {
            cached = cached(typeElement);
            if (cached != null) {
                return cached;
            }
            env.log("caching type element alias: %s", created);
            CACHE.put(typeElement, created);
            return created;
        }
    }

    static TypeElementAlias cached(TypeElement typeElement) {
        synchronized (CACHE) {
            return CACHE.get(typeElement);
        }
    }

    private static TypeElementAlias create(TypeElement element, Env env) {
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

        List<DeclaredTypeAlias> interfaceTypes = new ArrayList<>(interfaces.size());
        for (TypeMirror interfaceType : interfaces) {
            DeclaredTypeAlias elementType = DeclaredTypeAlias.parse(interfaceType, env);
            if (elementType != null) {
                env.log("interface type: %s", interfaceType);
                interfaceTypes.add(elementType);
            }
        }

        List<? extends Element> enclosedElements = element.getEnclosedElements();
        List<FunctionElement> functions = new ArrayList<>(enclosedElements.size());
        for (Element enclosed : enclosedElements) {
            FunctionElement functionElement = FunctionElement.parse(element, enclosed, env);
            if (functionElement == null) {
                continue;
            }
            functions.add(functionElement);
        }

        DeclaredTypeAlias superClass = DeclaredTypeAlias.parse(element.getSuperclass(), env);
        return new TypeElementAlias(env, element, superClass, typeVariables, interfaceTypes, functions);
    }

    private static TypeElementAlias objectElement(Env env) {
        TypeElementAlias oe;
        if ((oe = OBJECT_ELEMENT) == null) {
            synchronized (CACHE) {
                if ((oe = OBJECT_ELEMENT) == null) {
                    oe = OBJECT_ELEMENT = new TypeElementAlias(env);
                    CACHE.put(oe.element, oe);
                }
            }
        }
        return oe;
    }

    public Env env() {
        return env;
    }

    public TypeElement element() {
        return element;
    }

    public ElementKind kind() {
        return kind;
    }

    public ClassName className() {
        return className;
    }

    public String packageName() {
        return className.packageName();
    }

    public DeclaredTypeAlias superClass() {
        return superClass;
    }

    public ImmutableList<TypeVariable> typeVariables() {
        return typeVariables;
    }

    public ImmutableList<DeclaredTypeAlias> interfaceTypes() {
        return interfaceTypes;
    }

    public ImmutableList<FunctionElement> functions() {
        return functions;
    }

    protected boolean isInList(List<? extends DeclaredTypeAlias> typeElementAliases) {
        for (DeclaredTypeAlias typeAlias : typeElementAliases) {
            if (typeAlias.element().equals(this)) {
                return true;
            }
        }
        return false;
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TypeElementAlias)) return false;
        TypeElementAlias that = (TypeElementAlias) o;
        return Objects.equal(element, that.element);
    }

    @Override public int hashCode() {
        return Objects.hashCode(element);
    }

    @Override public String toString() {
        if (this == OBJECT_ELEMENT) {
            return MoreObjects.toStringHelper(this)
                    .add("\nelement", element)
                    .toString();
        }
        return MoreObjects.toStringHelper(this)
                .add("\nelement", element)
                .add("\nclassName", className)
                .add("\ntypeVariables", typeVariables)
                .add("\ninterfaceTypes", interfaceTypes)
                .add("\nfunctions", functions)
                .toString();
    }
}