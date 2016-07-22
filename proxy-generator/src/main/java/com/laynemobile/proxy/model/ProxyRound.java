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
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.laynemobile.proxy.annotations.GenerateProxyBuilder;
import com.laynemobile.proxy.annotations.Generated;
import com.laynemobile.proxy.elements.AliasElements;
import com.laynemobile.proxy.elements.TypeElementAlias;
import com.laynemobile.proxy.internal.ProxyLog;
import com.squareup.javapoet.JavaFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.Filer;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

import sourcerer.processor.Env;

public class ProxyRound extends Env {
    private final int round;
    private final ProxyRound previousRound;
    private final ImmutableSet<ProxyElement> proxyElements;
    private final ImmutableSet<ProxyElement> processedElements;
    private final ImmutableMap<ProxyElement, ImmutableList<GeneratedTypeElement>> inputs;
    private final ImmutableMap<ProxyElement, ImmutableList<GeneratedTypeElementStub>> outputStubs;
    private final ImmutableMap<TypeMirror, TypeElementAlias> temp;

    private ProxyRound(Env env) {
        super(env);
        this.round = 1;
        this.previousRound = null;
        this.proxyElements = ImmutableSet.of();
        this.processedElements = ImmutableSet.of();
        this.inputs = ImmutableMap.of();
        this.outputStubs = ImmutableMap.of();
        this.temp = ImmutableMap.of();
    }

    private ProxyRound(ProxyRound previousRound, Set<ProxyElement> proxyElements,
            Map<ProxyElement, ImmutableList<GeneratedTypeElement>> inputs,
            ImmutableMap<ProxyElement, ImmutableList<GeneratedTypeElementStub>> outputStubs,
            Map<TypeMirror, TypeElementAlias> temp) {
        super(previousRound);
        this.round = previousRound.round + 1;
        this.previousRound = previousRound;
        this.proxyElements = ImmutableSet.<ProxyElement>builder()
                .addAll(previousRound.proxyElements)
                .addAll(proxyElements)
                .build();
        this.processedElements = ImmutableSet.<ProxyElement>builder()
                .addAll(previousRound.processedElements)
                .addAll(outputStubs.keySet())
                .build();
        this.inputs = ImmutableMap.<ProxyElement, ImmutableList<GeneratedTypeElement>>builder()
                .putAll(previousRound.inputs)
                .putAll(inputs)
                .build();
        this.outputStubs = outputStubs;
        this.temp = ImmutableMap.copyOf(temp);
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

            processed |= add(proxyElements, element);
        }

        if (processed) {
            // log cached values:
            ImmutableList<? extends ProxyElement> cachedValues = ProxyElement.cache().values();

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

        List<TypeElementAlias> typeElementAliases = new ArrayList<>();
        for (Element element : roundEnv.getElementsAnnotatedWith(Generated.class)) {
            log("generated element: %s", element);
            if (element.getKind().isClass() || element.getKind().isInterface()) {
                TypeElementAlias typeElementAlias = AliasElements.get((TypeElement) element);
                typeElementAliases.add(typeElementAlias);
            }
        }

        Map<ProxyElement, ImmutableList<GeneratedTypeElement>> inputs = new HashMap<>();
        for (Map.Entry<ProxyElement, ImmutableList<GeneratedTypeElementStub>> entry : outputStubs.entrySet()) {
            ProxyElement key = entry.getKey();
            TypeElement typeElement = key.element();

            ImmutableList.Builder<GeneratedTypeElement> typeInputs = ImmutableList.builder();
            for (GeneratedTypeElementStub outputStub : entry.getValue()) {
                log("creating input from stub: %s", outputStub);
                GeneratedTypeElement typeInput = outputStub.generatedOutput(this);
                log("input: %s", typeInput);
                typeInputs.add(typeInput);

                GeneratedTypeElementStub inout = typeInput.output(this);
                try {
                    inout.newJavaFile()
                            .build()
                            .writeTo(filer());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            inputs.put(entry.getKey(), typeInputs.build());
        }

        try {
            ImmutableMap<ProxyElement, ImmutableList<GeneratedTypeElementStub>> outputs
                    = write(inputs, round);
            // add all processed elements from current round
            return new ProxyRound(this, proxyElements, inputs, outputs, temp);
        } catch (IOException e) {
            error("error writing: %s", ProxyLog.getStackTraceString(e));
            return null;
        }
    }

    private boolean add(Set<ProxyElement> proxyElements, Element element) {
        ProxyElement proxyElement = ProxyElement.cache().parse(element, this);
        return proxyElement != null && proxyElements.add(proxyElement);
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

    private ImmutableMap<ProxyElement, ImmutableList<GeneratedTypeElementStub>> write(
            Map<ProxyElement, ? extends List<GeneratedTypeElement>> inputs, Set<ProxyElement> round)
            throws IOException {
        Filer filer = filer();
        ImmutableMap.Builder<ProxyElement, ImmutableList<GeneratedTypeElementStub>> outputStubs
                = ImmutableMap.builder();
        for (ProxyElement output : round) {
            List<GeneratedTypeElement> dependencies = new ArrayList<>();

            ImmutableList.Builder<GeneratedTypeElementStub> functionOutputs
                    = ImmutableList.builder();
            for (ProxyFunctionElement functionElement : output.functions()) {
                GeneratedTypeElementStub outputStub = functionElement.output();
                JavaFile abstractProxyFunctionClass = outputStub.newJavaFile()
                        .build();
                log("writing %s -> \n%s", outputStub.qualifiedName(), abstractProxyFunctionClass.toString());
                abstractProxyFunctionClass.writeTo(filer);
                functionOutputs.add(outputStub);
            }
            outputStubs.put(output, functionOutputs.build());
        }
        return outputStubs.build();
    }

    @Override public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("round", round)
                .add("inputs", inputs.values())
                .add("outputStubs", outputStubs.values())
                .toString();
    }
}
