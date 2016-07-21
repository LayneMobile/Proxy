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
import com.google.common.collect.ImmutableSet;
import com.laynemobile.proxy.Util;
import com.laynemobile.proxy.annotations.GenerateProxyBuilder;
import com.laynemobile.proxy.cache.AliasCache;
import com.laynemobile.proxy.cache.AliasSubtypeCache;
import com.laynemobile.proxy.functions.Func0;
import com.squareup.javapoet.ClassName;

import java.util.List;
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;

import sourcerer.processor.Env;

public final class ProxyElement extends AbstractValueAlias<TypeElementAlias>
        implements Comparable<ProxyElement> {

    private final boolean parent;
    private final ImmutableList<TypeElementAlias> dependsOn;
    private final TypeElementAlias replaces;
    private final TypeElementAlias extendsFrom;
    private final ImmutableSet<ProxyElement> directDependencies;
    private final ImmutableList<ProxyFunctionElement> functions;

    private ProxyElement(TypeElementAlias source, boolean parent, List<TypeElementAlias> dependsOn,
            TypeElementAlias replaces, TypeElementAlias extendsFrom, Set<ProxyElement> directDependencies, Env env) {
        super(source);
        this.parent = parent;
        this.dependsOn = ImmutableList.copyOf(dependsOn);
        this.replaces = replaces;
        this.extendsFrom = extendsFrom;
        this.directDependencies = ImmutableSet.copyOf(directDependencies);
        this.functions = ProxyFunctionElement.parse(source, env);
    }

    public static AliasCache<TypeElement, ? extends ProxyElement, Element> cache() {
        return Cache.INSTANCE;
    }

    public final TypeElementAlias alias() {
        return value();
    }

    public final TypeElement element() {
        return value().element();
    }

    public final ClassName className() {
        return value().className();
    }

    public String packageName() {
        return value().packageName();
    }

    public boolean isParent() {
        return parent;
    }

    public ImmutableList<TypeElementAlias> dependsOn() {
        return dependsOn;
    }

    public TypeElementAlias replaces() {
        return replaces;
    }

    public TypeElementAlias extendsFrom() {
        return extendsFrom;
    }

    public ImmutableSet<ProxyElement> directDependencies() {
        return directDependencies;
    }

    public ImmutableList<ProxyFunctionElement> functions() {
        return functions;
    }

    @Override public int compareTo(ProxyElement o) {
        TypeElement element = element();
        if (equals(o) || element.equals(o.element())) {
            System.out.printf("'%s' equals '%s'\n", className(), o.className());
            return 0;
        } else if (o.dependsOn(this)) {
            System.out.printf("'%s' dependsOn '%s'\n", o.className(), this.className());
            return -1;
        } else if (this.dependsOn(o)) {
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
                .add("element", element())
                .toString();
    }

    public ImmutableSet<ProxyElement> allDependencies() {
        ImmutableSet.Builder<ProxyElement> dependencies = ImmutableSet.builder();
        for (ProxyElement dependency : directDependencies) {
            dependencies.add(dependency);
            dependencies.addAll(dependency.allDependencies());
        }
        return dependencies.build();
    }

    public String toDebugString() {
        return MoreObjects.toStringHelper(this)
                .add("element", element())
                .add("\nparent", parent)
                .add("\ndependsOn", dependsOn)
                .add("\nreplaces", replaces)
                .add("\nextendsFrom", extendsFrom)
                .toString();
    }

    boolean dependsOn(ProxyElement o) {
        if (directDependencies.contains(o)) {
            return true;
        }
        for (ProxyElement dependency : directDependencies) {
            if (dependency.dependsOn(o)) {
                return true;
            }
        }
        return false;
    }

    private static final class Cache extends AliasSubtypeCache<TypeElement, ProxyElement, Element, TypeElementAlias> {
        private static final Cache INSTANCE = new Cache();

        private Cache() {
            super(TypeElementAlias.cache());
        }

        @Override protected TypeElement cast(Element element, Env env) throws Exception {
            // Only interfaces allowed
            if (element.getKind() != ElementKind.INTERFACE) {
                return null;
            }
            return super.cast(element, env);
        }

        @Override protected ProxyElement create(TypeElementAlias source, Env env) {
            final GenerateProxyBuilder annotation = source.element().getAnnotation(GenerateProxyBuilder.class);
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

            ImmutableSet.Builder<ProxyElement> dependencies = ImmutableSet.builder();
            ProxyElement dependency;
            if ((dependency = dependency(source, replaces, env)) != null) {
                dependencies.add(dependency);
            }
            if ((dependency = dependency(source, extendsFrom, env)) != null) {
                dependencies.add(dependency);
            }
            for (TypeElementAlias alias : dependsOn) {
                if ((dependency = dependency(source, alias, env)) != null) {
                    dependencies.add(dependency);
                }
            }
            DeclaredTypeAlias superType = source.superClass();
            if (superType != null) {
                if ((dependency = dependency(source, superType.element(), env)) != null) {
                    dependencies.add(dependency);
                }
            }
            for (DeclaredTypeAlias typeAlias : source.interfaceTypes()) {
                if ((dependency = dependency(source, typeAlias.element(), env)) != null) {
                    dependencies.add(dependency);
                }
            }

            ProxyElement proxyElement
                    = new ProxyElement(source, parent, dependsOn, replaces, extendsFrom, dependencies.build(), env);
            env.log("created proxyElement: %s\n\n", proxyElement.toDebugString());
            return proxyElement;
        }

        private ProxyElement dependency(TypeElementAlias source, TypeElementAlias elementAlias, Env env) {
            if (elementAlias != null && !source.equals(elementAlias)) {
                return getOrCreate(elementAlias.element(), env);
            }
            return null;
        }
    }
}
