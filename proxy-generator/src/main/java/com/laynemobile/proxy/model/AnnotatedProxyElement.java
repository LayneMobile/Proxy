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
import com.google.common.collect.ImmutableSet;
import com.laynemobile.proxy.Util.Transformer;
import com.laynemobile.proxy.annotations.GenerateProxyBuilder;
import com.laynemobile.proxy.cache.ParameterizedCache;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import sourcerer.processor.Env;

import static com.laynemobile.proxy.Util.buildSet;

public final class AnnotatedProxyElement extends AbstractValueAlias<ProxyElement> {
    private final Env env;
    private final GenerateProxyBuilder annotation;
    private final ImmutableSet<AnnotatedProxyType> annotatedDirectDependencies;
    private final ImmutableSet<ProxyType> unannotatedDirectDependencies;
    private final ImmutableSet<AnnotatedProxyType> annotatedParamDependencies;
    private final ImmutableSet<ProxyType> unannotatedParamDependencies;
    private final ImmutableSet<AnnotatedProxyElement> annotatedOverrides;
    private final ImmutableSet<ProxyElement> unannotatedOverrides;
    private final ImmutableList<ProxyFunctionElement> functions;

    private AnnotatedProxyElement(ProxyElement element, GenerateProxyBuilder annotation, Env env) {
        super(element);
        Set<ProxyElement> overrides = new HashSet<>(element.overrides());
        for (ProxyType dependency : unannotatedTypes(element.allDependencies(), env)) {
            ProxyElement dependencyElement = dependency.element();
            if (dependencyElement.element().getKind() == ElementKind.INTERFACE) {
                overrides.add(dependencyElement);
            }
        }
        ImmutableSet<ProxyElement> unannotatedOverrides = unannotatedElements(overrides, env);
        LinkedHashSet<ProxyFunctionElement> functions = new LinkedHashSet<>(element.functions());

        TypeMirror type = element.element().asType().actual();
        if (type != null && type.getKind() == TypeKind.DECLARED) {
            DeclaredType containing = (DeclaredType) type;
            for (ProxyElement unannotatedOverride : unannotatedOverrides) {
                for (ProxyFunctionElement overrideFunction : unannotatedOverride.functions()) {
                    ExecutableElement base = overrideFunction.element().actual();
                    TypeMirror method = env.types().asMemberOf(containing, base);
                    env.log("override type: '%s', parent: '%s', method: '%s'", containing,
                            unannotatedOverride.element(), method);
                }
            }
        }

        this.env = env;
        this.annotation = annotation;
        this.annotatedDirectDependencies = annotatedTypes(element.directDependencies(), env);
        this.unannotatedDirectDependencies = unannotatedTypes(element.directDependencies(), env);
        this.annotatedParamDependencies = annotatedTypes(element.paramDependencies(), env);
        this.unannotatedParamDependencies = unannotatedTypes(element.paramDependencies(), env);
        this.annotatedOverrides = annotatedElements(overrides, env);
        this.unannotatedOverrides = unannotatedOverrides;
        this.functions = ImmutableList.copyOf(functions);
    }

    public static ParameterizedCache<ProxyElement, AnnotatedProxyElement, Env> cache() {
        return ElementCache.INSTANCE;
    }

    public static ImmutableSet<AnnotatedProxyElement> process(final Env env, RoundEnvironment roundEnv)
            throws IOException {
        try {
            Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(GenerateProxyBuilder.class);
            return buildSet(elements, new Transformer<AnnotatedProxyElement, Element>() {
                @Override public AnnotatedProxyElement transform(Element element) {
                    // Ensure it is an interface element
                    if (element.getKind() != ElementKind.INTERFACE) {
                        env.error(element, "Only interfaces can be annotated with @%s",
                                GenerateProxyBuilder.class.getSimpleName());
                        throw new RuntimeException("error");
                    }
                    return ElementCache.INSTANCE.parse(element, env);
                }
            });
        } catch (Throwable e) {
            throw new IOException(e);
        }
    }

    public ProxyElement element() {
        return value();
    }

    public GenerateProxyBuilder annotation() {
        return annotation;
    }

    public ImmutableSet<AnnotatedProxyType> annotatedDirectDependencies() {
        return annotatedDirectDependencies;
    }

    public ImmutableSet<ProxyType> unannotatedDirectDependencies() {
        return unannotatedDirectDependencies;
    }

    public ImmutableSet<AnnotatedProxyType> annotatedParamDependencies() {
        return annotatedParamDependencies;
    }

    public ImmutableSet<ProxyType> unannotatedParamDependencies() {
        return unannotatedParamDependencies;
    }

    public ImmutableSet<AnnotatedProxyElement> annotatedOverrides() {
        return annotatedOverrides;
    }

    public ImmutableSet<ProxyElement> unannotatedOverrides() {
        return unannotatedOverrides;
    }

    public ImmutableSet<AnnotatedProxyType> allAnnotatedDependencies() {
        return annotatedTypes(element().allDependencies(), env);
    }

    public ImmutableSet<ProxyType> allUnannotatedDependencies() {
        return unannotatedTypes(element().allDependencies(), env);
    }

    public ImmutableList<ProxyFunctionElement> functions() {
        return functions;
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AnnotatedProxyElement)) return false;
        return super.equals(o);
    }

    @Override public int hashCode() {
        return super.hashCode();
    }

    @Override public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("element", element())
                .toString();
    }

    private static ImmutableSet<AnnotatedProxyType> annotatedTypes(Set<? extends ProxyType> proxyTypes, final Env env) {
        return buildSet(proxyTypes, new Transformer<AnnotatedProxyType, ProxyType>() {
            @Override public AnnotatedProxyType transform(ProxyType proxyType) {
                return AnnotatedProxyType.cache().getOrCreate(proxyType, env);
            }
        });
    }

    private static ImmutableSet<ProxyType> unannotatedTypes(Set<? extends ProxyType> proxyTypes, final Env env) {
        return buildSet(proxyTypes, new Transformer<ProxyType, ProxyType>() {
            @Override public ProxyType transform(ProxyType proxyType) {
                if (AnnotatedProxyType.cache().getOrCreate(proxyType, env) == null) {
                    return proxyType;
                }
                return null;
            }
        });
    }

    private static ImmutableSet<AnnotatedProxyElement> annotatedElements(Set<? extends ProxyElement> proxyElements,
            final Env env) {
        return buildSet(proxyElements, new Transformer<AnnotatedProxyElement, ProxyElement>() {
            @Override public AnnotatedProxyElement transform(ProxyElement proxyElement) {
                return cache().getOrCreate(proxyElement, env);
            }
        });
    }

    private static ImmutableSet<ProxyElement> unannotatedElements(Set<? extends ProxyElement> proxyElements,
            final Env env) {
        return buildSet(proxyElements, new Transformer<ProxyElement, ProxyElement>() {
            @Override public ProxyElement transform(ProxyElement proxyElement) {
                if (cache().getOrCreate(proxyElement, env) == null) {
                    return proxyElement;
                }
                return null;
            }
        });
    }

    private static final class ElementCache implements ParameterizedCache<ProxyElement, AnnotatedProxyElement, Env> {
        private static final ElementCache INSTANCE = new ElementCache();

        private final Map<ProxyElement, AnnotatedProxyElement> cache = new HashMap<>();

        private ElementCache() {}

        @Override public AnnotatedProxyElement getOrCreate(ProxyElement proxyElement, Env env) {
            AnnotatedProxyElement cached;
            if ((cached = getIfPresent(proxyElement)) == null) {
                AnnotatedProxyElement created;
                if ((created = create(proxyElement, env)) != null && (cached = getIfPresent(proxyElement)) == null) {
                    synchronized (cache) {
                        cache.put(proxyElement, created);
                        return created;
                    }
                }
            }
            return cached;
        }

        @Override public AnnotatedProxyElement get(ProxyElement proxyElement) {
            return getIfPresent(proxyElement);
        }

        private AnnotatedProxyElement parse(Element element, Env env) {
            // Ensure it is an interface element
            if (element.getKind() == ElementKind.INTERFACE) {
                ProxyElement proxyElement = ProxyElement.cache()
                        .parse(element, env);
                if (proxyElement != null) {
                    return getOrCreate(proxyElement, env);
                }
            }
            return null;
        }

        private AnnotatedProxyElement create(ProxyElement proxyElement, Env env) {
            GenerateProxyBuilder annotation;
            if (proxyElement != null
                    && (annotation = proxyElement.element().getAnnotation(GenerateProxyBuilder.class)) != null) {
                return new AnnotatedProxyElement(proxyElement, annotation, env);
            }
            return null;
        }

        private AnnotatedProxyElement getIfPresent(ProxyElement proxyElement) {
            synchronized (cache) {
                return cache.get(proxyElement);
            }
        }

        @Override public ImmutableList<AnnotatedProxyElement> values() {
            synchronized (cache) {
                return ImmutableList.copyOf(cache.values());
            }
        }
    }
}
