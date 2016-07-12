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
import com.laynemobile.proxy.annotations.GenerateProxyBuilder;
import com.squareup.javapoet.JavaFile;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.processing.Filer;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;

import sourcerer.processor.Env;

public class ProxyRound extends Env {
    private final int round;
    private final ImmutableSet<ProxyElement> proxyElements;
    private final ImmutableSet<ProxyElement> processedElements;
    private final ProxyRound previousRound;

    private ProxyRound(Env env) {
        super(env);
        this.round = 1;
        this.proxyElements = ImmutableSet.of();
        this.processedElements = ImmutableSet.of();
        this.previousRound = null;
    }

    private ProxyRound(ProxyRound previousRound, Set<ProxyElement> proxyElements, Set<ProxyElement> round) {
        super(previousRound);
        this.round = previousRound.round + 1;
        this.proxyElements = ImmutableSet.<ProxyElement>builder()
                .addAll(previousRound.proxyElements)
                .addAll(proxyElements)
                .build();
        this.processedElements = ImmutableSet.<ProxyElement>builder()
                .addAll(previousRound.processedElements)
                .addAll(round)
                .build();
        this.previousRound = previousRound;
    }

    public static ProxyRound begin(Env env) {
        return new ProxyRound(env);
    }

    public boolean isFirstRound() {
        return previousRound == null;
    }

    public ProxyRound process(RoundEnvironment roundEnv) {
        boolean processed = false;
        Set<ProxyElement> proxyElements = new HashSet<>(this.proxyElements);
        for (Element element : roundEnv.getElementsAnnotatedWith(GenerateProxyBuilder.class)) {
            // Ensure it is an interface element
            if (element.getKind() != ElementKind.INTERFACE) {
                error(element, "Only interfaces can be annotated with @%s",
                        GenerateProxyBuilder.class.getSimpleName());
                return null; // Exit processing
            }

            if (!add(proxyElements, element)) {
                return null; // Exit processing
            }
            processed = true;
        }

        if (processed) {
            // log cached values:
            ImmutableList<ProxyElement> cachedValues = ImmutableList.copyOf(ProxyElement.cache().values());

            log("cached proxy elements: %s", cachedValues);

            for (ProxyElement proxyElement : cachedValues) {
                if (!proxyElements.contains(proxyElement)) {
                    log("adding new cached proxy element: %s", proxyElement);
                    proxyElements.add(proxyElement);
                }
            }
        }

        Set<ProxyElement> round = new HashSet<>();
        Set<ProxyElement> dependencies = new HashSet<>();
        ImmutableSet<ProxyElement> unprocessedElements = unprocessed(proxyElements);
        for (ProxyElement unprocessed : unprocessedElements) {
            for (ProxyElement dependency : unprocessed.allDependencies()) {
                if (!processedElements.contains(dependency)) {
                    dependencies.add(dependency);
                }
            }
        }
        for (ProxyElement unprocessed : unprocessedElements) {
            boolean hasDependency = false;
            for (ProxyElement dependency : unprocessed.allDependencies()) {
                if (dependencies.contains(dependency)) {
                    hasDependency = true;
                    break;
                }
            }
            if (!hasDependency) {
                round.add(unprocessed);
            }
        }

        try {
            write(round);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        // add all processed elements from current round
        return new ProxyRound(this, proxyElements, round);
    }

    private boolean add(Set<ProxyElement> proxyElements, Element element) {
        ProxyElement proxyElement = ProxyElement.cache().parse(element, this);
        if (proxyElement == null) {
            return false;
        }
        proxyElements.add(proxyElement);
        return true;
    }

    private ImmutableSet<ProxyElement> unprocessed(Set<ProxyElement> proxyElements) {
        ImmutableSet<ProxyElement> processedElements = this.processedElements;
        ImmutableSet.Builder<ProxyElement> unprocessed = ImmutableSet.builder();
        for (ProxyElement proxyElement : proxyElements) {
            if (!processedElements.contains(proxyElement)) {
                unprocessed.add(proxyElement);
            }
        }
        return unprocessed.build();
    }

    private void write(Collection<ProxyElement> proxyElements) throws IOException {
        Filer filer = filer();
        for (ProxyElement proxyElement : proxyElements) {
            for (ProxyFunctionElement functionElement : proxyElement.functions()) {
                GeneratedTypeElementStub output = functionElement.output();
                JavaFile abstractProxyFunctionClass = output.newJavaFile()
                        .build();
                log("writing AbstractProxyFunctionClass -> \n" + abstractProxyFunctionClass.toString());
                abstractProxyFunctionClass.writeTo(filer);
            }
        }
    }

    @Override public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("round", round)
                .toString();
    }
}
