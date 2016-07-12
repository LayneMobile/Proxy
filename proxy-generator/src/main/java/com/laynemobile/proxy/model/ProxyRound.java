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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.laynemobile.proxy.annotations.GenerateProxyBuilder;
import com.squareup.javapoet.JavaFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.processing.Filer;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;

import sourcerer.processor.Env;

public class ProxyRound extends Env {
    private final Set<ProxyElement> proxyElements = new TreeSet<>();
    private final Set<ProxyElement> processedElements = new TreeSet<>();
    private final ProxyRound previousRound;

    private ProxyRound(Env env) {
        super(env);
        this.previousRound = null;
    }

    private ProxyRound(ProxyRound previousRound) {
        super(previousRound);
        synchronized (previousRound.proxyElements) {
            this.proxyElements.addAll(previousRound.proxyElements);
        }
        synchronized (previousRound.processedElements) {
            this.processedElements.addAll(previousRound.processedElements);
        }
        this.previousRound = previousRound;
    }

    public static ProxyRound begin(Env env) {
        return new ProxyRound(env);
    }

    public boolean isFirstRound() {
        return previousRound == null;
    }

    public ProxyRound beginNextRound() {
        return new ProxyRound(this);
    }

    public boolean process(RoundEnvironment roundEnv) {
        boolean processed = false;
        for (Element element : roundEnv.getElementsAnnotatedWith(GenerateProxyBuilder.class)) {
            // Ensure it is an interface element
            if (element.getKind() != ElementKind.INTERFACE) {
                error(element, "Only interfaces can be annotated with @%s",
                        GenerateProxyBuilder.class.getSimpleName());
                return true; // Exit processing
            }

            if (!add(element)) {
                return false; // Exit processing
            }
            processed = true;
        }

        if (processed) {
            // log cached values:
            ImmutableList<ProxyElement> cachedValues = ImmutableList.copyOf(ProxyElement.cache().values());

            log("cached proxy elements: %s", cachedValues);

            synchronized (proxyElements) {
                for (ProxyElement proxyElement : cachedValues) {
                    if (!proxyElements.contains(proxyElement)) {
                        log("adding new cached proxy element: %s", proxyElement);
                        proxyElements.add(proxyElement);
                    }
                }
            }
        }

        Set<ProxyElement> round = new HashSet<>();
        Set<ProxyElement> dependencies = new HashSet<>();
        ImmutableSet<ProxyElement> unprocessedElements = unprocessed();
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
            return false;
        }

        // add all processed elements (current round)
        synchronized (processedElements) {
            processedElements.addAll(round);
        }

        return true;
    }

    private boolean add(Element element) {
        ProxyElement proxyElement = ProxyElement.cache().parse(element, this);
        if (proxyElement == null) {
            return false;
        }
        synchronized (proxyElements) {
            if (!proxyElements.contains(proxyElement)) {
                proxyElements.add(proxyElement);
            }
        }
        return true;
    }

    private List<ProxyElement> proxyElements() {
        synchronized (proxyElements) {
            return new ArrayList<>(proxyElements);
        }
    }

    private ImmutableSet<ProxyElement> unprocessed() {
        ImmutableSet.Builder<ProxyElement> unprocessed = ImmutableSet.builder();
        synchronized (processedElements) {
            for (ProxyElement proxyElement : proxyElements()) {
                if (!processedElements.contains(proxyElement)) {
                    unprocessed.add(proxyElement);
                }
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
}
