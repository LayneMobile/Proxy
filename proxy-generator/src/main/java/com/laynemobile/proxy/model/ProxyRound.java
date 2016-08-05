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
import com.laynemobile.proxy.Util.Transformer;
import com.laynemobile.proxy.annotations.GenerateProxyBuilder;
import com.laynemobile.proxy.internal.Util;
import com.laynemobile.proxy.model.output.ProxyHandlerBuilderOutputStub;
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
                .add("\ninput", doubleIndent(input))
                .add("\noutput", doubleIndent(output))
                .toString();
    }

    private static ImmutableSet<ProxyElement> processProxyElements(final Env env, RoundEnvironment roundEnv)
            throws IOException {
        try {
            Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(GenerateProxyBuilder.class);
            return buildSet(elements, new Transformer<ProxyElement, Element>() {
                @Override public ProxyElement transform(Element element) {
                    // Ensure it is an interface element
                    if (element.getKind() != ElementKind.INTERFACE) {
                        env.error(element, "Only interfaces can be annotated with @%s",
                                GenerateProxyBuilder.class.getSimpleName());
                        throw new RuntimeException("error");
                    }
                    return ProxyElement.cache().parse(element, env);
                }
            });
        } catch (Throwable e) {
            throw new IOException(e);
        }
    }

    public static final class Input extends EnvRound<Input> {
        private final ImmutableSet<? extends TypeElement> annotations;
        private final ImmutableSet<? extends Element> rootElements;
        private final ImmutableSet<ProxyElement> proxyElements;
        private final ImmutableSet<ProxyElement> outputElements;
        private final ImmutableMap<ProxyElement, ImmutableSet<TypeElementOutputStub>> inputs;
        private final ImmutableMap<ProxyElement, ProxyHandlerBuilderOutputStub> handlerStubs;

        private Input(Env env) {
            super(env);
            this.annotations = ImmutableSet.of();
            this.rootElements = ImmutableSet.of();
            this.proxyElements = ImmutableSet.of();
            this.outputElements = ImmutableSet.of();
            this.inputs = ImmutableMap.of();
            this.handlerStubs = ImmutableMap.of();
        }

        private Input(Input previous, Set<? extends TypeElement> annotations, Set<? extends Element> rootElements,
                Set<? extends ProxyElement> proxyElements, Set<? extends ProxyElement> outputElements,
                Map<ProxyElement, ImmutableSet<TypeElementOutputStub>> inputs,
                Map<ProxyElement, ProxyHandlerBuilderOutputStub> handlerStubs) {
            super(previous);
            this.annotations = ImmutableSet.copyOf(annotations);
            this.rootElements = ImmutableSet.copyOf(rootElements);
            this.proxyElements = ImmutableSet.copyOf(proxyElements);
            this.outputElements = ImmutableSet.copyOf(outputElements);
            this.inputs = ImmutableMap.copyOf(inputs);
            this.handlerStubs = ImmutableMap.copyOf(handlerStubs);
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

        public ImmutableMap<ProxyElement, ImmutableSet<TypeElementOutputStub>> inputs() {
            return inputs;
        }

        public ImmutableMap<ProxyElement, ProxyHandlerBuilderOutputStub> handlerStubs() {
            return handlerStubs;
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

        public ImmutableMap<ProxyElement, ImmutableSet<TypeElementOutputStub>> allInputs() {
            Input previous = previous();
            if (previous == null) {
                return inputs;
            }
            return combine(previous.allInputs(), inputs);
        }

        public ImmutableMap<ProxyElement, ProxyHandlerBuilderOutputStub> allHandlerStubs() {
            ImmutableMap.Builder<ProxyElement, ProxyHandlerBuilderOutputStub> out = ImmutableMap.builder();
            for (Input input : allRounds()) {
                out.putAll(input.handlerStubs);
            }
            return out.build();
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

        private Input process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv, Output lastOutput)
                throws IOException {
            final ProxyEnv env = env();
            final ImmutableSet<ProxyElement> curProxyElements = processProxyElements(env, roundEnv);
            final ImmutableSet<ProxyElement> allProxyElements = ImmutableSet.<ProxyElement>builder()
                    .addAll(allProxyElements())
                    .addAll(curProxyElements)
                    .build();
            final Map<ProxyElement, ImmutableSet<TypeElementOutputStub>> inputStubs
                    = new HashMap<>(lastOutput.nextInputStubs(env));
            log("all proxy elements: %s", allProxyElements);

            ImmutableMap<ProxyElement, ProxyHandlerBuilderOutputStub> allHandlerStubs = allHandlerStubs();

            // add builder elements
            Map<ProxyElement, ProxyHandlerBuilderOutputStub> handlerStubs = new HashMap<>();
            for (ProxyElement p : lastOutput.outputs.keySet()) {
                if (!allHandlerStubs.containsKey(p) && !inputStubs.containsKey(p)) {
                    ProxyHandlerBuilderOutputStub builderStub = ProxyHandlerBuilderOutputStub.create(p);
                    handlerStubs.put(p, builderStub);
                    inputStubs.put(p, ImmutableSet.<TypeElementOutputStub>of(builderStub));
                }
            }

            final ImmutableSet<ProxyElement> unprocessedElements = unprocessed(allProxyElements);
            log("all unprocessed elements: %s", unprocessedElements);

            final Set<ProxyElement> processedElements = new HashSet<>(allOutputElements());
            processedElements.removeAll(inputStubs.keySet());

            final Set<ProxyType> dependencies = buildSet(unprocessedElements, new Collector<ProxyType, ProxyElement>() {
                @Override public void collect(ProxyElement unprocessed, ImmutableCollection.Builder<ProxyType> out) {
                    for (ProxyType dependency : unprocessed.allDependencies()) {
                        if (!processedElements.contains(dependency.element())) {
                            out.add(dependency);
                        }
                    }
                }
            });

            log("dependencies: %s", dependencies);

            Set<ProxyElement> round = buildSet(unprocessedElements, new Transformer<ProxyElement, ProxyElement>() {
                @Override public ProxyElement transform(ProxyElement unprocessed) {
                    for (ProxyType dependency : unprocessed.allDependencies()) {
                        if (dependencies.contains(dependency)) {
                            return null;
                        }
                    }
                    return unprocessed;
                }
            });

            return new Input(this, annotations, roundEnv.getRootElements(), curProxyElements, round, inputStubs,
                    handlerStubs);
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
        private final ImmutableMap<ProxyElement, ImmutableSet<TypeElementOutputStub>> outputStubs;
        private final ImmutableMap<ProxyElement, ImmutableSet<TypeElementOutput>> outputs;

        private Output() {
            this.outputStubs = ImmutableMap.of();
            this.outputs = ImmutableMap.of();
        }

        private Output(Output previous,
                Map<ProxyElement, ImmutableSet<TypeElementOutputStub>> outputStubs,
                ImmutableMap<ProxyElement, ImmutableSet<TypeElementOutput>> outputs) {
            super(previous);
            this.outputStubs = ImmutableMap.copyOf(outputStubs);
            this.outputs = outputs;
        }

        Output write(Input input) throws IOException {
            final ProxyEnv env = input.env();
            final Map<ProxyElement, ImmutableSet<TypeElementOutputStub>> outputStubs = new HashMap<>(input.inputs());

            for (ProxyElement proxyElement : input.outputElements()) {
                Set<TypeElementOutputStub> curr = Util.nullSafe(outputStubs.remove(proxyElement));
                outputStubs.put(proxyElement, ImmutableSet.<TypeElementOutputStub>builder()
                        .addAll(proxyElement.outputs(input, env))
                        .addAll(curr)
                        .build());
            }

            ImmutableMap.Builder<ProxyElement, ImmutableSet<TypeElementOutput>> outputs = ImmutableMap.builder();
            for (Map.Entry<ProxyElement, ? extends Set<TypeElementOutputStub>> inputEntry : outputStubs.entrySet()) {
                ImmutableSet.Builder<TypeElementOutput> set = ImmutableSet.builder();
                for (TypeElementOutputStub typeInput : inputEntry.getValue()) {
                    TypeElementOutput output = typeInput.writeTo(env);
                    if (output != null) {
                        set.add(output);
                    }
                }
                outputs.put(inputEntry.getKey(), set.build());
            }
            return new Output(this, outputStubs, outputs.build());
        }

        public ImmutableMap<ProxyElement, ImmutableSet<TypeElementOutputStub>> outputStubs() {
            return outputStubs;
        }

        public ImmutableMap<ProxyElement, ImmutableSet<TypeElementOutput>> outputs() {
            return outputs;
        }

        public ImmutableMap<ProxyElement, ImmutableSet<TypeElementOutputStub>> allOutputStubs() {
            Output previous = previous();
            if (previous == null) {
                return outputStubs;
            }
            return combine(previous.allOutputStubs(), outputStubs);
        }

        public ImmutableMap<ProxyElement, ImmutableSet<TypeElementOutput>> allOutputs() {
            Output previous = previous();
            if (previous == null) {
                return outputs;
            }
            return combine(previous.allOutputs(), outputs);
        }

        public ImmutableMap<ProxyElement, ImmutableSet<TypeElementOutputStub>> nextInputStubs(Env env) {
            ImmutableMap.Builder<ProxyElement, ImmutableSet<TypeElementOutputStub>> inputStubs = ImmutableMap.builder();
            for (Map.Entry<ProxyElement, ? extends Set<TypeElementOutput>> inputEntry : outputs.entrySet()) {
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
                    inputStubs.put(inputEntry.getKey(), set.build());
                }
            }
            return inputStubs.build();
        }

        @Override public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("\noutputStubs", outputStubs)
                    .add("\noutputs", outputs)
                    .toString();
        }
    }

    static String doubleIndent(Object o) {
        return doubleIndent(o == null ? null : o.toString());
    }

    static String doubleIndent(String value) {
        if (value == null) {
            return "null";
        }
        return value.replace("\n", "\n    ");
    }
}
