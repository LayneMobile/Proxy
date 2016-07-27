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
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.laynemobile.proxy.Util.Collector;
import com.laynemobile.proxy.annotations.GenerateProxyBuilder;
import com.laynemobile.proxy.internal.Util;
import com.laynemobile.proxy.model.output.TypeElementOutput;
import com.laynemobile.proxy.model.output.TypeElementOutputStub;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.Messager;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

import sourcerer.processor.Env;

import static com.laynemobile.proxy.Util.buildSet;
import static com.laynemobile.proxy.Util.combine;

public class ProxyRound extends Env implements Iterable<ProxyRound> {
    private static final Messager MESSAGER = new Messager() {
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

    private final int round;
    private final ProxyRound previous;
    private final ImmutableSet<? extends TypeElement> annotations;
    private final ImmutableSet<? extends Element> rootElements;
    private final ImmutableSet<ProxyElement> proxyElements;
    private final ImmutableSet<ProxyElement> processedElements;
    private final ImmutableMap<ProxyElement, ImmutableSet<TypeElementOutput>> outputs;

    private ProxyRound(Env env) {
        super(env);
        this.round = 0;
        this.previous = null;
        this.annotations = ImmutableSet.of();
        this.rootElements = ImmutableSet.of();
        this.proxyElements = ImmutableSet.of();
        this.processedElements = ImmutableSet.of();
        this.outputs = ImmutableMap.of();
    }

    private ProxyRound(ProxyRound previous, Set<? extends TypeElement> annotations,
            Set<? extends Element> rootElements, Set<? extends ProxyElement> proxyElements,
            Set<? extends ProxyElement> processedElements,
            Map<ProxyElement, ImmutableSet<TypeElementOutput>> outputs) {
        super(previous);
        this.round = previous.round + 1;
        this.previous = previous;
        this.annotations = ImmutableSet.copyOf(annotations);
        this.rootElements = ImmutableSet.copyOf(rootElements);
        this.proxyElements = ImmutableSet.copyOf(proxyElements);
        this.processedElements = ImmutableSet.copyOf(processedElements);
        this.outputs = ImmutableMap.copyOf(outputs);
    }

    static ProxyRound begin(Env env) {
        return new ProxyRound(env);
    }

    @Override public Messager messager() {
        return MESSAGER;
    }

    public boolean isFirstRound() {
        return previous == null;
    }

    ProxyRound process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) throws IOException {
        Set<ProxyElement> allProxyElements = new HashSet<>(allProxyElements());
        log("all proxy elements: %s", allProxyElements);
        Set<ProxyElement> proxyElements = new HashSet<>();
        for (Element element : roundEnv.getElementsAnnotatedWith(GenerateProxyBuilder.class)) {
            // Ensure it is an interface element
            if (element.getKind() != ElementKind.INTERFACE) {
                error(element, "Only interfaces can be annotated with @%s",
                        GenerateProxyBuilder.class.getSimpleName());
                return null; // Exit processing
            }
            ProxyElement proxyElement = ProxyElement.cache().parse(element, this);
            if (proxyElement != null) {
                proxyElements.add(proxyElement);
                allProxyElements.add(proxyElement);
            }
        }

        Set<ProxyElement> round = new HashSet<>();
        Set<ProxyType> dependencies = new HashSet<>();
        Set<ProxyElement> allProcessedElements = allProcessedElements();
        ImmutableSet<ProxyElement> unprocessedElements = unprocessed(allProxyElements);
        for (ProxyElement unprocessed : unprocessedElements) {
            for (ProxyType dependency : unprocessed.allDependencies()) {
                if (!allProcessedElements.contains(dependency.element())) {
                    dependencies.add(dependency);
                }
            }
        }
        log("all unprocessed elements: %s", unprocessedElements);
        log("dependencies: %s", dependencies);
        OUTER:
        for (ProxyElement unprocessed : unprocessedElements) {
            for (ProxyType dependency : unprocessed.allDependencies()) {
                if (dependencies.contains(dependency)) {
                    continue OUTER;
                }
            }
            round.add(unprocessed);
        }

        ImmutableMap<ProxyElement, ImmutableSet<TypeElementOutput>> outputs = write(round);
        // add all processed elements from current round
        return new ProxyRound(this, annotations, roundEnv.getRootElements(), proxyElements, round, outputs);
    }

    @Override public Iterator<ProxyRound> iterator() {
        return new StateIterator(this);
    }

    private ImmutableSet<ProxyElement> unprocessed(Set<ProxyElement> proxyElements) {
        ImmutableSet<ProxyElement> allProcessedElements = allProcessedElements();
        ImmutableSet.Builder<ProxyElement> unprocessed = ImmutableSet.builder();
        for (ProxyElement proxyElement : proxyElements) {
            if (!allProcessedElements.contains(proxyElement)) {
                unprocessed.add(proxyElement);
            }
            for (ProxyType dependency : proxyElement.allDependencies()) {
                ProxyElement element = dependency.element();
                if (!allProcessedElements.contains(element)) {
                    unprocessed.add(element);
                }
            }
        }
        return unprocessed.build();
    }

    private ImmutableMap<ProxyElement, ImmutableSet<TypeElementOutput>> write(Set<ProxyElement> round)
            throws IOException {
        Map<ProxyElement, ImmutableSet<TypeElementOutput>> outputs = new HashMap<>();
        for (Map.Entry<ProxyElement, ? extends Set<TypeElementOutput>> inputEntry : this.outputs.entrySet()) {
            ImmutableSet.Builder<TypeElementOutput> set = ImmutableSet.builder();
            for (TypeElementOutput typeInput : inputEntry.getValue()) {
                if (typeInput.hasOutput()) {
                    TypeElementOutputStub inout = typeInput.outputStub(this);
                    TypeElementOutput output = inout.writeTo(this);
                    if (output != null) {
                        set.add(output);
                    }
                }
            }
            outputs.put(inputEntry.getKey(), set.build());
        }

        Map<ProxyElement, ImmutableSet<TypeElementOutput>> inputs
                = combine(allOutputs(), outputs);
        for (ProxyElement proxyElement : round) {
            Set<TypeElementOutput> curr = Util.nullSafe(outputs.remove(proxyElement));
            ImmutableSet.Builder<TypeElementOutput> set = ImmutableSet.<TypeElementOutput>builder()
                    .addAll(curr);
            for (TypeElementOutputStub outputStub : proxyElement.outputs(inputs, this)) {
                TypeElementOutput output = outputStub.writeTo(this);
                if (output != null) {
                    set.add(output);
                }
            }
            outputs.put(proxyElement, set.build());
        }
        return ImmutableMap.copyOf(outputs);
    }

    ImmutableList<ProxyRound> allRounds() {
        return ImmutableList.copyOf(iterator());
    }

    ImmutableSet<Element> allRootElements() {
        return buildSet(allRounds(), new Collector<Element, ProxyRound>() {
            @Override public void collect(ProxyRound proxyRound, ImmutableCollection.Builder<Element> out) {
                out.addAll(proxyRound.rootElements);
            }
        });
    }

    ImmutableSet<ProxyElement> allProxyElements() {
        return buildSet(allRounds(), new Collector<ProxyElement, ProxyRound>() {
            @Override public void collect(ProxyRound proxyRound, ImmutableCollection.Builder<ProxyElement> out) {
                out.addAll(proxyRound.proxyElements);
            }
        });
    }

    ImmutableSet<ProxyElement> allProcessedElements() {
        return buildSet(allRounds(), new Collector<ProxyElement, ProxyRound>() {
            @Override public void collect(ProxyRound proxyRound, ImmutableCollection.Builder<ProxyElement> out) {
                out.addAll(proxyRound.processedElements);
            }
        });
    }

    ImmutableMap<ProxyElement, ImmutableSet<TypeElementOutput>> allOutputs() {
        if (previous == null) {
            return outputs;
        }
        return combine(previous.allOutputs(), outputs);
    }

    @Override public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("\nround", round)
                .add("\nannotations", annotations)
                .add("\nrootElements", rootElements)
                .add("\nproxyElements", proxyElements)
                .add("\nprocessedElements", processedElements)
                .add("\noutputs", outputs)
                .toString();
    }

    private static class StateIterator implements Iterator<ProxyRound> {
        private ProxyRound proxyRound;

        private StateIterator(ProxyRound proxyRound) {
            this.proxyRound = proxyRound;
        }

        @Override public boolean hasNext() {
            return proxyRound != null;
        }

        @Override public ProxyRound next() {
            ProxyRound next = proxyRound;
            proxyRound = proxyRound.previous;
            return next;
        }

        @Override public void remove() {
            throw new UnsupportedOperationException("immutable");
        }
    }
}
