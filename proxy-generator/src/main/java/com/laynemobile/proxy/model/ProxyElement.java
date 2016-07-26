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
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.laynemobile.proxy.Util;
import com.laynemobile.proxy.annotations.GenerateProxyBuilder;
import com.laynemobile.proxy.cache.AliasCache;
import com.laynemobile.proxy.elements.AliasElements;
import com.laynemobile.proxy.elements.AnnotationMirrorAlias;
import com.laynemobile.proxy.elements.TypeElementAlias;
import com.laynemobile.proxy.functions.Func0;
import com.laynemobile.proxy.model.ProxyFunctionElement.FunctionParentOutputStub;
import com.laynemobile.proxy.types.DeclaredTypeAlias;
import com.laynemobile.proxy.types.TypeMirrorAlias;
import com.squareup.javapoet.ClassName;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import sourcerer.processor.Env;

public final class ProxyElement extends AbstractValueAlias<TypeElementAlias>
        implements Comparable<ProxyElement> {

    private final boolean parent;
    private final ClassName className;
    private final ImmutableList<TypeElementAlias> dependsOn;
    private final TypeElementAlias replaces;
    private final TypeElementAlias extendsFrom;
    private final ImmutableSet<ProxyType> directDependencies;
    private final ImmutableList<ProxyFunctionElement> functions;
    private final ImmutableSet<ProxyType> paramDependencies;

    private ProxyElement(TypeElementAlias source, boolean parent, ClassName className,
            List<TypeElementAlias> dependsOn,
            TypeElementAlias replaces, TypeElementAlias extendsFrom, Set<ProxyType> directDependencies, Env env) {
        super(source);
        ImmutableList<ProxyFunctionElement> functions = ProxyFunctionElement.parse(source, env);
        ImmutableSet.Builder<ProxyType> paramDependencies = ImmutableSet.builder();
        for (ProxyFunctionElement function : functions) {
            MethodElement method = function.value();
            for (TypeMirrorAlias paramType : method.paramTypes()) {
                Element element = env.types().asElement(paramType.actual());
                if (element == null
                        || element.getKind() != ElementKind.INTERFACE
                        || source.actual().equals(element)) {
                    continue;
                }
                ProxyType proxyType = ProxyType.cache().parse(paramType, env);
                if (proxyType != null) {
                    paramDependencies.add(proxyType);
                }
            }
        }

        this.parent = parent;
        this.className = className;
        this.dependsOn = ImmutableList.copyOf(dependsOn);
        this.replaces = replaces;
        this.extendsFrom = extendsFrom;
        this.directDependencies = ImmutableSet.copyOf(directDependencies);
        this.functions = ProxyFunctionElement.parse(source, env);
        this.paramDependencies = paramDependencies.build();
    }

    public static AliasCache<TypeElementAlias, ? extends ProxyElement, Element> cache() {
        return Cache.INSTANCE;
    }

    public TypeElementAlias element() {
        return value();
    }

    public ClassName className() {
        return className;
    }

    public String packageName() {
        return className.packageName();
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

    public ImmutableSet<ProxyType> directDependencies() {
        return directDependencies;
    }

    public ImmutableList<ProxyFunctionElement> functions() {
        return functions;
    }

    private static final Util.ArrayCreator<TypeMirror> TYPE_MIRROR_ARRAY_CREATOR = new Util.ArrayCreator<TypeMirror>() {
        @Override public TypeMirror[] newArray(int size) {
            return new TypeMirror[size];
        }
    };

    public ImmutableList<GeneratedTypeElementStub> outputs(
            final Map<ProxyElement, ? extends List<GeneratedTypeElement>> inputs, final Env env) {
        return Util.buildList(functions(), new Util.Transformer<GeneratedTypeElementStub, ProxyFunctionElement>() {
            @Override public GeneratedTypeElementStub transform(ProxyFunctionElement functionElement) {
                final FunctionParentOutputStub outputStub = functionElement.output();
                for (ProxyFunctionElement override : functionElement.overrides()) {
                    ProxyElement overrideParentElement = override.parent();
                    List<GeneratedTypeElement> generated = inputs.get(overrideParentElement);
                    if (generated != null && !generated.isEmpty()) {
                        env.log("say man");
                        env.log("%s -- writing override '%s' from '%s' -- %s", toDebugString(),
                                outputStub.qualifiedName(), override, generated);
                        env.log("say man");

                        ProxyType overrideParentType = null;
                        for (ProxyType test : directDependencies()) {
                            if (test.element().equals(overrideParentElement)) {
                                overrideParentType = test;
                                break;
                            }
                        }
                        if (overrideParentType == null) {
                            continue;
                        }

                        TypeMirror[] typeParams = Util.toArray(overrideParentType.type().actual().getTypeArguments(),
                                TYPE_MIRROR_ARRAY_CREATOR);
                        final TypeElement superElement = generated.get(0).value().actual();
                        env.log("super proxy element type parameters: %s", Arrays.toString(typeParams));
                        DeclaredType superType = env.types()
                                .getDeclaredType(superElement, typeParams);
                        return outputStub.withSuperClass(superType);
                    }
                }
                return outputStub;
            }
        });
    }

    @Override public int compareTo(ProxyElement o) {
        TypeElementAlias element = element();
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

    public ImmutableSet<ProxyType> allDependencies() {
        ImmutableSet<ProxyType> dependencies = ImmutableSet.<ProxyType>builder()
                .addAll(directDependencies)
                .addAll(paramDependencies)
                .build();
        return Util.buildSet(dependencies, new Util.Collector<ProxyType, ProxyType>() {
            @Override public void collect(ProxyType dependency, ImmutableCollection.Builder<ProxyType> out) {
                out.add(dependency);
                out.addAll(dependency.element().allDependencies());
            }
        });
    }

    public String toDebugString() {
        return MoreObjects.toStringHelper(this)
                .add("element", element())
                .add("\nparent", parent)
                .add("\ndependsOn", dependsOn)
                .add("\nreplaces", replaces)
                .add("\nextendsFrom", extendsFrom)
                .add("\ndirectDependencies", directDependencies)
                .add("\nparamDependencies", paramDependencies)
                .toString();
    }

    boolean dependsOn(ProxyElement o) {
        return listDependsOn(directDependencies, o)
                || listDependsOn(paramDependencies, o);
    }

    private boolean listDependsOn(Collection<? extends ProxyType> dependencies, ProxyElement o) {
        for (ProxyType dependency : dependencies) {
            if (dependency.element().equals(o)) {
                return true;
            }
            if (dependency.element().dependsOn(o)) {
                return true;
            }
        }
        return false;
    }

    private static final class Cache extends AliasCache<TypeElementAlias, ProxyElement, Element> {
        private static final Cache INSTANCE = new Cache();

        private Cache() {}

        @Override protected TypeElementAlias cast(Element element, Env env) throws Exception {
            log(env, "casting element: %s", element);
            // Only interfaces and classes allowed
            if (element.getKind() != ElementKind.INTERFACE && element.getKind() != ElementKind.CLASS) {
//            // Only interfaces allowed
//            if (element.getKind() != ElementKind.INTERFACE) {
                return null;
            }
            return AliasElements.get((TypeElement) element);
        }

        @Override protected ProxyElement create(TypeElementAlias source, Env env) {
            TypeElement te;
            for (AnnotationMirrorAlias ama : source.getAnnotationMirrors()) {
                log(env, "annotation mirror: %s", ama);
                DeclaredTypeAlias type = ama.getAnnotationType();
                // TODO:
            }

            final GenerateProxyBuilder annotation = source.getAnnotation(GenerateProxyBuilder.class);
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

            ImmutableSet.Builder<ProxyType> dependencies = ImmutableSet.builder();
            ProxyType dependency;
            TypeMirrorAlias superType = source.getSuperclass();
            if (superType != null) {
                if ((dependency = dependency(source, superType, env)) != null) {
                    dependencies.add(dependency);
                }
            }
            for (TypeMirrorAlias typeAlias : source.getInterfaces()) {
                if ((dependency = dependency(source, typeAlias, env)) != null) {
                    dependencies.add(dependency);
                }
            }

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

            ClassName className = ClassName.bestGuess(source.getQualifiedName().toString());
            ProxyElement proxyElement
                    = new ProxyElement(source, parent, className, dependsOn, replaces, extendsFrom,
                    dependencies.build(), env);
            env.log("created proxyElement: %s\n\n", proxyElement.toDebugString());
            return proxyElement;
        }

        private ProxyType dependency(TypeElementAlias source, DeclaredTypeAlias typeAlias, Env env) {
            if (typeAlias != null && !source.equals(typeAlias.asElement())) {
                return ProxyType.cache().getOrCreate(typeAlias, env);
            }
            return null;
        }

        private ProxyType dependency(TypeElementAlias source, TypeMirrorAlias typeAlias, Env env) {
            if (typeAlias.getKind() == TypeKind.DECLARED) {
                return dependency(source, (DeclaredTypeAlias) typeAlias, env);
            }
            return null;
        }

        private ProxyType dependency(TypeElementAlias source, Element element, Env env) {
            if (element == null) return null;
            if (element.getKind().isClass() || element.getKind().isInterface()) {
                return dependency(source, AliasElements.get((TypeElement) element).asType(), env);
            }
            return null;
        }
    }
}
