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
import com.laynemobile.proxy.elements.TypeElementAlias;
import com.laynemobile.proxy.internal.ProxyLog;
import com.laynemobile.proxy.internal.Util;
import com.squareup.javapoet.JavaFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;

import sourcerer.processor.Env;

public class ProxyRound extends Env {
    private final int round;
    private final ProxyRound previousRound;
    private final ImmutableSet<ProxyElement> proxyElements;
    private final ImmutableSet<ProxyElement> processedElements;
    private final ImmutableMap<ProxyElement, ImmutableList<GeneratedTypeElement>> inputs;
    private final ImmutableMap<ProxyElement, ImmutableList<GeneratedTypeElementStub>> outputStubs;
    private final ImmutableMap<TypeMirror, TypeElementAlias> temp;

    private final Messager messager = new Messager() {
        @Override public void printMessage(Diagnostic.Kind kind, CharSequence msg) {
            System.out.printf("%s: %s\n", kind, msg);
        }

        @Override public void printMessage(Diagnostic.Kind kind, CharSequence msg, Element e) {
            System.out.printf("%s: e='%s' - %s\n", kind, e, msg);
        }

        @Override public void printMessage(Diagnostic.Kind kind, CharSequence msg, Element e, AnnotationMirror a) {
            System.out.printf("%s: e='%s', a='%s' - %s\n", kind, e, a, msg);
        }

        @Override
        public void printMessage(Diagnostic.Kind kind, CharSequence msg, Element e, AnnotationMirror a,
                AnnotationValue v) {
            System.out.printf("%s: e='%s', a='%s', v='%s' - %s\n", kind, e, a, v, msg);
        }
    };

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
        ImmutableMap.Builder<ProxyElement, ImmutableList<GeneratedTypeElement>> allInputs
                = ImmutableMap.builder();
        inputs = new HashMap<>(inputs);
        for (Map.Entry<ProxyElement, ? extends List<GeneratedTypeElement>> entry : previousRound.inputs.entrySet()) {
            ProxyElement key = entry.getKey();
            List<GeneratedTypeElement> value = inputs.remove(key);
            allInputs.put(key, ImmutableList.<GeneratedTypeElement>builder()
                    .addAll(value)
                    .addAll(entry.getValue())
                    .build());
        }
        for (Map.Entry<ProxyElement, ImmutableList<GeneratedTypeElement>> entry : inputs.entrySet()) {
            allInputs.put(entry.getKey(), entry.getValue());
        }
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
        this.inputs = allInputs.build();
        this.outputStubs = outputStubs;
        this.temp = ImmutableMap.copyOf(temp);
    }

    @Override public Messager messager() {
        return messager;
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

//        for (TypeElementAlias typeElementAlias : TypeElementAlias.cache().values()) {
//            log("cached type element: %s", typeElementAlias);
//        }

        Set<ProxyElement> round = new HashSet<>();
        Set<ProxyType> dependencies = new HashSet<>();
        ImmutableSet<ProxyElement> unprocessedElements = unprocessed(proxyElements);
        for (ProxyElement unprocessed : unprocessedElements) {
            for (ProxyType dependency : unprocessed.allDependencies()) {
                if (!processedElements.contains(dependency.element())) {
                    dependencies.add(dependency);
                }
            }
        }
        OUTER:
        for (ProxyElement unprocessed : unprocessedElements) {
            for (ProxyType dependency : unprocessed.allDependencies()) {
                if (dependencies.contains(dependency)) {
                    continue OUTER;
                }
            }
            round.add(unprocessed);
        }

//        List<TypeElementAlias> typeElementAliases = new ArrayList<>();
//        for (Element element : roundEnv.getElementsAnnotatedWith(Generated.class)) {
//            log("generated element: %s", element);
//            if (element.getKind().isClass() || element.getKind().isInterface()) {
//                TypeElementAlias typeElementAlias = AliasElements.get((TypeElement) element);
//                typeElementAliases.add(typeElementAlias);
//            }
//        }

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
        Map<ProxyElement, ImmutableList<GeneratedTypeElementStub>> outputStubs = new HashMap<>();
        for (Map.Entry<ProxyElement, ? extends List<GeneratedTypeElement>> inputEntry : inputs.entrySet()) {
            ImmutableList.Builder<GeneratedTypeElementStub> inOutputs = ImmutableList.builder();
            for (GeneratedTypeElement typeInput : inputEntry.getValue()) {
                if (typeInput.hasOutput()) {
                    GeneratedTypeElementStub inout = typeInput.output(this);
                    JavaFile javaFile = inout.newJavaFile()
                            .build();
                    log("writing %s -> \n%s", inout.qualifiedName(), javaFile.toString());
                    javaFile.writeTo(filer());
                    inOutputs.add(inout);
                }
            }
            outputStubs.put(inputEntry.getKey(), inOutputs.build());
        }
        for (ProxyElement output : round) {
            ImmutableList<GeneratedTypeElementStub> functionOutputs = output.outputs(inputs, this);
            for (GeneratedTypeElementStub outputStub : functionOutputs) {
                JavaFile abstractProxyFunctionClass = outputStub.newJavaFile()
                        .build();
                log("writing %s -> \n%s", outputStub.qualifiedName(), abstractProxyFunctionClass.toString());
                abstractProxyFunctionClass.writeTo(filer);
            }
            List<GeneratedTypeElementStub> curr = Util.nullSafe(outputStubs.get(output));
            outputStubs.put(output, ImmutableList.<GeneratedTypeElementStub>builder()
                    .addAll(functionOutputs)
                    .addAll(curr)
                    .build());
        }
        return ImmutableMap.copyOf(outputStubs);
    }

    @Override public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("round", round)
                .add("inputs", inputs.values())
                .add("outputStubs", outputStubs.values())
                .toString();
    }
}
