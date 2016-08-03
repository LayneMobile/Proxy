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
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;

import sourcerer.processor.Env;

import static com.laynemobile.proxy.Util.buildSet;
import static com.laynemobile.proxy.Util.combine;

public class ProxyRound extends EnvRound<ProxyRound> {
    private final Input input;
    private final Output output;

    private ProxyRound(Env env) {
        super(env);
        this.input = new Input(env);
        this.output = new Output();
    }

    private ProxyRound(ProxyRound previous, Input input, Output output) {
        super(previous);
        this.input = input;
        this.output = output;
    }

    static ProxyRound begin(Env env) {
        return new ProxyRound(env);
    }

    @Override protected ProxyRound current() {
        return this;
    }

    ProxyRound process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) throws IOException {
        // add all processed elements from current round
        Output lastOutput = output;
        Input newInput = input.process(annotations, roundEnv, lastOutput);
        Output newOutput = lastOutput.write(newInput);
        return new ProxyRound(this, newInput, newOutput);
    }

    public Input input() {
        return input;
    }

    public Output output() {
        return output;
    }

    @Override public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("\nround", round())
                .add("\ninput", input)
                .add("\noutput", output)
                .toString();
    }

    public static final class Input extends EnvRound<Input> {
        private final ImmutableSet<? extends TypeElement> annotations;
        private final ImmutableSet<? extends Element> rootElements;
        private final ImmutableSet<ProxyElement> proxyElements;
        private final ImmutableSet<ProxyElement> outputElements;
        private final ImmutableMap<ProxyElement, ImmutableSet<TypeElementOutputStub>> outputStubs;

        private Input(Env env) {
            super(env);
            this.annotations = ImmutableSet.of();
            this.rootElements = ImmutableSet.of();
            this.proxyElements = ImmutableSet.of();
            this.outputElements = ImmutableSet.of();
            this.outputStubs = ImmutableMap.of();
        }

        private Input(Input previous, Set<? extends TypeElement> annotations, Set<? extends Element> rootElements,
                Set<? extends ProxyElement> proxyElements, Set<? extends ProxyElement> outputElements,
                Map<ProxyElement, ImmutableSet<TypeElementOutputStub>> outputStubs) {
            super(previous);
            this.annotations = ImmutableSet.copyOf(annotations);
            this.rootElements = ImmutableSet.copyOf(rootElements);
            this.proxyElements = ImmutableSet.copyOf(proxyElements);
            this.outputElements = ImmutableSet.copyOf(outputElements);
            this.outputStubs = ImmutableMap.copyOf(outputStubs);
        }

        @Override protected Input current() {
            return this;
        }

        public ImmutableSet<? extends TypeElement> annotations() {
            return annotations;
        }

        public ImmutableSet<? extends Element> rootElements() {
            return rootElements;
        }

        public ImmutableSet<ProxyElement> proxyElements() {
            return proxyElements;
        }

        public ImmutableSet<ProxyElement> outputElements() {
            return outputElements;
        }

        public ImmutableMap<ProxyElement, ImmutableSet<TypeElementOutputStub>> outputStubs() {
            return outputStubs;
        }

        public ImmutableSet<Element> allRootElements() {
            return buildSet(allRounds(), new Collector<Element, Input>() {
                @Override public void collect(Input input, ImmutableCollection.Builder<Element> out) {
                    out.addAll(input.rootElements);
                }
            });
        }

        public ImmutableSet<ProxyElement> allProxyElements() {
            return buildSet(allRounds(), new Collector<ProxyElement, Input>() {
                @Override public void collect(Input input, ImmutableCollection.Builder<ProxyElement> out) {
                    out.addAll(input.proxyElements);
                }
            });
        }

        public ImmutableSet<ProxyElement> allOutputElements() {
            return buildSet(allRounds(), new Collector<ProxyElement, Input>() {
                @Override public void collect(Input input, ImmutableCollection.Builder<ProxyElement> out) {
                    out.addAll(input.outputElements);
                }
            });
        }

        public ImmutableMap<ProxyElement, ImmutableSet<TypeElementOutputStub>> allOutputStubs() {
            Input previous = previous();
            if (previous == null) {
                return outputStubs;
            }
            return combine(previous.allOutputStubs(), outputStubs);
        }

        public ImmutableSet<ProxyElement> allProcessedElements() {
            Input previous = previous();
            if (previous == null) {
                return ImmutableSet.of();
            }
            return buildSet(previous.allRounds(), new Collector<ProxyElement, Input>() {
                @Override public void collect(Input input, ImmutableCollection.Builder<ProxyElement> out) {
                    out.addAll(input.outputElements);
                }
            });
        }

        private ImmutableSet<ProxyElement> unprocessed(Set<ProxyElement> proxyElements) {
            Set<ProxyElement> processedElements = allOutputElements();
            ImmutableSet.Builder<ProxyElement> unprocessed = ImmutableSet.builder();
            for (ProxyElement proxyElement : proxyElements) {
                if (!processedElements.contains(proxyElement)) {
                    unprocessed.add(proxyElement);
                }
                for (ProxyType dependency : proxyElement.allDependencies()) {
                    ProxyElement element = dependency.element();
                    if (!processedElements.contains(element)) {
                        unprocessed.add(element);
                    }
                }
            }
            return unprocessed.build();
        }

        private Input process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv,
                Output lastOutput) throws IOException {
            final ProxyEnv env = env();
            Set<ProxyElement> allProxyElements = new HashSet<>(allProxyElements());
            log("all proxy elements: %s", allProxyElements);
            Set<ProxyElement> proxyElements = new HashSet<>();
            for (Element element : roundEnv.getElementsAnnotatedWith(GenerateProxyBuilder.class)) {
                // Ensure it is an interface element
                if (element.getKind() != ElementKind.INTERFACE) {
                    error(element, "Only interfaces can be annotated with @%s",
                            GenerateProxyBuilder.class.getSimpleName());
                    throw new IOException("error");
                }
                ProxyElement proxyElement = ProxyElement.cache().parse(element, env);
                if (proxyElement != null) {
                    proxyElements.add(proxyElement);
                    allProxyElements.add(proxyElement);
                }
            }

            Map<ProxyElement, ImmutableSet<TypeElementOutputStub>> outputStubs = new HashMap<>();
            for (Map.Entry<ProxyElement, ? extends Set<TypeElementOutput>> inputEntry : lastOutput.outputs.entrySet()) {
                ImmutableSet.Builder<TypeElementOutputStub> set = null;
                for (TypeElementOutput typeInput : inputEntry.getValue()) {
                    if (typeInput.hasOutput()) {
                        TypeElementOutputStub outputStub = typeInput.outputStub(env);
                        if (outputStub != null) {
                            if (set == null) {
                                set = ImmutableSet.builder();
                            }
                            set.add(outputStub);
                        }
                    }
                }
                if (set != null) {
                    outputStubs.put(inputEntry.getKey(), set.build());
                }
            }
            Set<ProxyElement> processedElements = new HashSet<>(allOutputElements());
            processedElements.removeAll(outputStubs.keySet());

            Set<ProxyElement> round = new HashSet<>();
            Set<ProxyType> dependencies = new HashSet<>();
            ImmutableSet<ProxyElement> unprocessedElements = unprocessed(allProxyElements);
            for (ProxyElement unprocessed : unprocessedElements) {
                for (ProxyType dependency : unprocessed.allDependencies()) {
                    if (!processedElements.contains(dependency.element())) {
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

            ImmutableMap<ProxyElement, ImmutableSet<TypeElementOutputStub>> inputs
                    = combine(allOutputStubs(), outputStubs);
            for (ProxyElement proxyElement : round) {
                Set<TypeElementOutputStub> curr = Util.nullSafe(outputStubs.remove(proxyElement));
                outputStubs.put(proxyElement, ImmutableSet.<TypeElementOutputStub>builder()
                        .addAll(proxyElement.outputs(inputs, env))
                        .addAll(curr)
                        .build());
            }
            return new Input(this, annotations, roundEnv.getRootElements(), proxyElements, round, outputStubs);
        }

        @Override public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("\nannotations", annotations)
                    .add("\nrootElements", rootElements)
                    .add("\nproxyElements", proxyElements)
                    .add("\noutputElements", outputElements)
                    .toString();
        }
    }

    public static final class Output extends AbstractRound<Output> {
        private final ImmutableMap<ProxyElement, ImmutableSet<TypeElementOutput>> outputs;

        private Output() {
            this.outputs = ImmutableMap.of();
        }

        private Output(Output previous, ImmutableMap<ProxyElement, ImmutableSet<TypeElementOutput>> outputs) {
            super(previous);
            this.outputs = outputs;
        }

        Output write(Input input) throws IOException {
            final ProxyEnv env = input.env();
            ImmutableMap.Builder<ProxyElement, ImmutableSet<TypeElementOutput>> outputs = ImmutableMap.builder();
            for (Map.Entry<ProxyElement, ? extends Set<TypeElementOutputStub>> inputEntry : input.outputStubs.entrySet()) {
                ImmutableSet.Builder<TypeElementOutput> set = ImmutableSet.builder();
                for (TypeElementOutputStub typeInput : inputEntry.getValue()) {
                    TypeElementOutput output = typeInput.writeTo(env);
                    if (output != null) {
                        set.add(output);
                    }
                }
                outputs.put(inputEntry.getKey(), set.build());
            }
            return new Output(this, outputs.build());
        }

        @Override protected Output current() {
            return this;
        }

        public ImmutableMap<ProxyElement, ImmutableSet<TypeElementOutput>> allOutputs() {
            Output previous = previous();
            if (previous == null) {
                return outputs;
            }
            return combine(previous.allOutputs(), outputs);
        }

        @Override public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("\noutputs", outputs)
                    .toString();
        }
    }
}
