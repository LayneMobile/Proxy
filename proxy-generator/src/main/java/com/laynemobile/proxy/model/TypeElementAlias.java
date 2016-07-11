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
import com.squareup.javapoet.ClassName;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;

import sourcerer.processor.Env;

public final class TypeElementAlias extends AbstractValueAlias<TypeElement> {
    private final ElementKind kind;
    private final ClassName className;
    private final DeclaredTypeAlias superClass;
    private final ImmutableList<TypeVariable> typeVariables;
    private final ImmutableList<DeclaredTypeAlias> interfaceTypes;
    private final ImmutableList<? extends MethodElement> methods;

    private TypeElementAlias(TypeElement typeElement, Env env) {
        super(typeElement);
        List<? extends TypeMirror> interfaces = typeElement.getInterfaces();
        List<? extends TypeParameterElement> typeParameters = typeElement.getTypeParameters();

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
            DeclaredTypeAlias elementType = DeclaredTypeAlias.cache().parse(interfaceType, env);
            if (elementType != null) {
                env.log("interface type: %s", interfaceType);
                interfaceTypes.add(elementType);
            }
        }

        DeclaredTypeAlias superClass = DeclaredTypeAlias.cache()
                .parse(typeElement.getSuperclass(), env);

        this.kind = typeElement.getKind();
        this.className = ClassName.get(typeElement);
        this.superClass = superClass;
        this.typeVariables = ImmutableList.copyOf(typeVariables);
        this.interfaceTypes = ImmutableList.copyOf(interfaceTypes);
        this.methods = MethodElement.parse(typeElement, env);
    }

    public static AliasCache<TypeElement, ? extends TypeElementAlias, Element> cache() {
        return Cache.INSTANCE;
    }

    public final TypeElement element() {
        return value();
    }

    public final ElementKind kind() {
        return kind;
    }

    public final ClassName className() {
        return className;
    }

    public final String packageName() {
        return className.packageName();
    }

    public final DeclaredTypeAlias superClass() {
        return superClass;
    }

    public final ImmutableList<TypeVariable> typeVariables() {
        return typeVariables;
    }

    public final ImmutableList<DeclaredTypeAlias> interfaceTypes() {
        return interfaceTypes;
    }

    public ImmutableList<? extends MethodElement> methods() {
        return methods;
    }

    protected final boolean isInList(List<? extends DeclaredTypeAlias> typeElementAliases) {
        for (DeclaredTypeAlias typeAlias : typeElementAliases) {
            if (typeAlias.element().equals(this)) {
                return true;
            }
        }
        return false;
    }

    @Override public boolean equals(Object o) {
        return this == o || o instanceof TypeElementAlias && super.equals(o);
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
                .add("\nelement", element())
                .add("\nclassName", className)
                .add("\ntypeVariables", typeVariables)
                .add("\ninterfaceTypes", interfaceTypes)
                .add("\nmethods", methods)
                .toString();
    }

    private static final class Cache extends AliasCache<TypeElement, TypeElementAlias, Element> {
        private static final Cache INSTANCE = new Cache();

        private Cache() {}

        @Override protected TypeElement cast(Element element) throws Exception {
            if (element.getKind() == ElementKind.CLASS || element.getKind() == ElementKind.INTERFACE) {
                return (TypeElement) element;
            }
            return null;
        }

        @Override protected TypeElementAlias create(TypeElement typeElement, Env env) {
            TypeElementAlias elementAlias = new TypeElementAlias(typeElement, env);
            env.log("created typeElementAlias: %s\n\n", elementAlias.toDebugString());
            return elementAlias;
        }
    }
}
