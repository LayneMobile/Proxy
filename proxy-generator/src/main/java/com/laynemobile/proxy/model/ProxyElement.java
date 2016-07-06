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
import com.laynemobile.proxy.cache.EnvCache;
import com.laynemobile.proxy.functions.Func0;

import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;

import sourcerer.processor.Env;

public final class ProxyElement extends TypeElementAlias implements Comparable<ProxyElement> {
    private final boolean parent;
    private final ImmutableList<? extends TypeElementAlias> dependsOn;
    private final TypeElementAlias replaces;
    private final TypeElementAlias extendsFrom;

    private ProxyElement(TypeElementAlias source, boolean parent, List<TypeElementAlias> dependsOn,
            TypeElementAlias replaces, TypeElementAlias extendsFrom) {
        super(source);
        this.parent = parent;
        this.dependsOn = ImmutableList.copyOf(dependsOn);
        this.replaces = replaces;
        this.extendsFrom = extendsFrom;
    }

    public static EnvCache<Element, TypeElement, ? extends ProxyElement> cache() {
        return Cache.INSTANCE;
    }

    public static ProxyElement from(TypeElementAlias source) {
        if (source.kind() == ElementKind.INTERFACE) {
            return cache().getOrCreate(source.element(), source.env());
        }
        return null;
    }

    public boolean isParent() {
        return parent;
    }

    public ImmutableList<? extends TypeElementAlias> dependsOn() {
        return dependsOn;
    }

    public TypeElementAlias replaces() {
        return replaces;
    }

    public TypeElementAlias extendsFrom() {
        return extendsFrom;
    }

    @Override public int compareTo(ProxyElement o) {
        TypeElement element = element();
        if (equals(o) || element.equals(o.element())) {
            System.out.printf("'%s' equals '%s'\n", className(), o.className());
            return 0;
        } else if (o.dependsOn(this)) {
            System.out.printf("'%s' dependsOn '%s'\n", o.className(), this.className());
            return -1;
        } else if (dependsOn(o)) {
            System.out.printf("'%s' dependsOn '%s'\n", className(), o.className());
            return 1;
        } else if (parent && !o.parent) {
            System.out.printf("'%s' is a parent\n", className());
            return -1;
        } else if (o.parent && !parent) {
            System.out.printf("'%s' is a parent\n", o.className());
            return 1;
        }
        System.out.printf("'%s' equals-compareName '%s'\n", className(), o.className());
        return element.getQualifiedName().toString()
                .compareTo(o.element().getQualifiedName().toString());
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProxyElement)) return false;
        if (!super.equals(o)) return false;
        ProxyElement that = (ProxyElement) o;
        return parent == that.parent &&
                Objects.equal(dependsOn, that.dependsOn) &&
                Objects.equal(replaces, that.replaces) &&
                Objects.equal(extendsFrom, that.extendsFrom);
    }

    @Override public int hashCode() {
        return Objects.hashCode(super.hashCode(), parent, dependsOn, replaces, extendsFrom);
    }

    @Override public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("super", className())
                .add("\nparent", parent)
                .add("\ndependsOn", dependsOn)
                .add("\nreplaces", replaces)
                .add("\nextendsFrom", extendsFrom)
                .toString();
    }

    boolean dependsOn(ProxyElement o) {
        return dependsOn.contains(o)
                || replaces.equals(o)
                || extendsFrom.equals(o)
                || o.isInList(interfaceTypes());
    }

    private static final class Cache extends EnvCache<Element, TypeElement, ProxyElement> {
        private static final Cache INSTANCE = new Cache();

        private Cache() {}

        @Override protected TypeElement cast(Element element) throws Exception {
            // Only interfaces allowed
            if (element.getKind() != ElementKind.INTERFACE) {
                return null;
            }
            return (TypeElement) element;
        }

        @Override protected ProxyElement create(TypeElement typeElement, Env env) {
            TypeElementAlias source = TypeElementAlias.cache().getOrCreate(typeElement, env);
            final GenerateProxyBuilder annotation = typeElement.getAnnotation(GenerateProxyBuilder.class);
            final boolean parent = annotation != null && annotation.parent();
            final List<TypeElementAlias> dependsOn = Util.parseAliasList(new Func0<Class<?>[]>() {
                @Override public Class<?>[] call() {
                    return annotation == null ? new Class[]{} : annotation.dependsOn();
                }
            }, env);
            final TypeElementAlias replaces = Util.parseAlias(new Func0<Class<?>>() {
                @Override public Class<?> call() {
                    return annotation == null ? Object.class : annotation.replaces();
                }
            }, env);
            final TypeElementAlias extendsFrom = Util.parseAlias(new Func0<Class<?>>() {
                @Override public Class<?> call() {
                    return annotation == null ? Object.class : annotation.extendsFrom();
                }
            }, env);
            return new ProxyElement(source, parent, dependsOn, replaces, extendsFrom);
        }
    }
}
