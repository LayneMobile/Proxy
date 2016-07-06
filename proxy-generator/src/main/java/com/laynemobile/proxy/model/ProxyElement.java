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
import com.laynemobile.proxy.Util;
import com.laynemobile.proxy.annotations.GenerateProxyBuilder;
import com.laynemobile.proxy.functions.Func0;
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

public final class ProxyElement implements Comparable<ProxyElement> {
    private static final Map<TypeElement, ProxyElement> CACHE = new HashMap<>();
    private static volatile ProxyElement OBJECT_ELEMENT;

    private final TypeElement element;
    private final ClassName className;
    private final Metadata metadata;
    private final ImmutableList<TypeVariable> typeVariables;
    private final ImmutableList<ProxyType> interfaceTypes;
    private final ImmutableList<FunctionElement> functions;

    /* java.lang.Object element constructor. */
    private ProxyElement(Env env) {
        this.element = env.elements().getTypeElement("java.lang.Object");
        this.className = ClassName.get(element);
        this.typeVariables = ImmutableList.of();
        this.interfaceTypes = ImmutableList.of();
        this.functions = ImmutableList.of();
        this.metadata = new Metadata(this);
    }

    private ProxyElement(TypeElement element, Metadata metadata, List<TypeVariable> typeVariables,
            List<ProxyType> interfaceTypes, List<FunctionElement> functions) {
        this.element = element;
        this.className = ClassName.get(element);
        this.metadata = metadata;
        this.typeVariables = ImmutableList.copyOf(typeVariables);
        this.interfaceTypes = ImmutableList.copyOf(interfaceTypes);
        this.functions = ImmutableList.copyOf(functions);
    }

    public static ProxyElement parse(Element element, Env env) {
        if (element.getKind() == ElementKind.CLASS) {
            // Only allow java.lang.Object class
            ProxyElement objectElement = objectElement(env);
            if (objectElement.element.equals(element)) {
                return objectElement;
            }
            return null;
        } else if (element.getKind() != ElementKind.INTERFACE) {
            return null;
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

        Metadata metadata = Metadata.parse(element, env);
        return new ProxyElement(element, metadata, typeVariables, interfaceTypes, functions);
    }

    private static ProxyElement objectElement(Env env) {
        ProxyElement oe;
        if ((oe = OBJECT_ELEMENT) == null) {
            synchronized (CACHE) {
                if ((oe = OBJECT_ELEMENT) == null) {
                    oe = OBJECT_ELEMENT = new ProxyElement(env);
                    CACHE.put(oe.element, oe);
                }
            }
        }
        return oe;
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

    public Metadata metadata() {
        return metadata;
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

    @Override public int compareTo(ProxyElement o) {
        if (equals(o) || element.equals(o.element)) {
            System.out.printf("\n\n'%s'\nequals\n'%s'\n\n", this, o);
            return 0;
        } else if (o.dependsOn(this)) {
            System.out.printf("\n\n'%s'\ndependsOn\n'%s'\n\n", o, this);
            return -1;
        } else if (dependsOn(o)) {
            System.out.printf("\n\n'%s'\ndependsOn\n'%s'\n\n", this, o);
            return 1;
        } else if (metadata.parent && !o.metadata.parent) {
            System.out.printf("\n\n'%s' is a parent\n\n", this);
            return -1;
        } else if (o.metadata.parent && !metadata.parent) {
            System.out.printf("\n\n'%s' is a parent\n\n", o);
            return 1;
        }
        System.out.printf("\n\n'%s'\nequals-compareName\n'%s'\n\n", this, o);
        return element.getQualifiedName().toString()
                .compareTo(o.element.getQualifiedName().toString());
    }

    boolean dependsOn(ProxyElement o) {
        return metadata.dependsOn.contains(o)
                || metadata.replaces.equals(o)
                || metadata.extendsFrom.equals(o)
                || o.isInList(interfaceTypes);
    }

    private boolean isInList(List<ProxyType> proxyTypes) {
        for (ProxyType proxyType : proxyTypes) {
            if (proxyType.element().equals(this)) {
                return true;
            }
        }
        return false;
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
        if (this == OBJECT_ELEMENT) {
            return MoreObjects.toStringHelper(this)
                    .add("\nelement", element)
                    .toString();
        }
        return MoreObjects.toStringHelper(this)
                .add("\nelement", element)
                .add("\nclassName", className)
                .add("\nmetadata", metadata)
                .add("\ntypeVariables", typeVariables)
                .add("\ninterfaceTypes", interfaceTypes)
                .add("\nfunctions", functions)
                .toString();
    }

    public static final class Metadata {
        private final boolean parent;
        private final ImmutableList<ProxyElement> dependsOn;
        private final ProxyElement replaces;
        private final ProxyElement extendsFrom;

        private Metadata(ProxyElement objectElement) {
            this.parent = true;
            this.dependsOn = ImmutableList.of(objectElement);
            this.replaces = objectElement;
            this.extendsFrom = objectElement;
        }

        private Metadata(boolean parent, List<ProxyElement> dependsOn, ProxyElement replaces,
                ProxyElement extendsFrom) {
            this.parent = parent;
            this.dependsOn = ImmutableList.copyOf(dependsOn);
            this.replaces = replaces;
            this.extendsFrom = extendsFrom;
        }

        private static Metadata parse(TypeElement element, Env env) {
            final GenerateProxyBuilder annotation = element.getAnnotation(GenerateProxyBuilder.class);
            final boolean parent = annotation != null && annotation.parent();
            final List<ProxyElement> dependsOn = Util.parseProxyList(new Func0<Class<?>[]>() {
                @Override public Class<?>[] call() {
                    return annotation == null ? new Class[]{} : annotation.dependsOn();
                }
            }, env);
            final ProxyElement replaces = Util.parseProxy(new Func0<Class<?>>() {
                @Override public Class<?> call() {
                    return annotation == null ? Object.class : annotation.replaces();
                }
            }, env);
            final ProxyElement extendsFrom = Util.parseProxy(new Func0<Class<?>>() {
                @Override public Class<?> call() {
                    return annotation == null ? Object.class : annotation.extendsFrom();
                }
            }, env);
            return new Metadata(parent, dependsOn, replaces, extendsFrom);
        }

        public boolean isParent() {
            return parent;
        }

        public ImmutableList<ProxyElement> dependsOn() {
            return dependsOn;
        }

        public ProxyElement replaces() {
            return replaces;
        }

        public ProxyElement extendsFrom() {
            return extendsFrom;
        }

        @Override public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("\nparent", parent)
                    .add("\ndependsOn", dependsOn)
                    .add("\nreplaces", replaces)
                    .add("\nextendsFrom", extendsFrom)
                    .toString();
        }
    }
}
