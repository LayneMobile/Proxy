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
import com.laynemobile.proxy.model.output.ProxyElementOutput;
import com.laynemobile.proxy.model.output.ProxyFunctionOutput;
import com.laynemobile.proxy.model.output.TypeElementOutput;

import java.io.IOException;
import java.util.HashSet;
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
        ProxyRound nextRound = new ProxyRound(this, newInput, newOutput);
        if (!newOutput.outputRounds().isEmpty() && !newOutput.didWrite()) {
            log("round=%s", nextRound);
            return nextRound.process(annotations, roundEnv);
        }
        return nextRound;
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

    private static boolean isInList(ProxyElement proxyElement, Set<ProxyElementOutput> list) {
        for (ProxyElementOutput item : list) {
            if (item.element().equals(proxyElement)) {
                return true;
            }
        }
        return false;
    }

    public static final class Input extends EnvRound<Input> {
        private final ImmutableSet<? extends TypeElement> annotations;
        private final ImmutableSet<? extends Element> rootElements;
        private final ImmutableSet<ProxyElement> proxyElements;
        private final ImmutableSet<ProxyElement> outputElements;
        private final ImmutableSet<ProxyElementRound> inputRounds;

        private Input(Env env) {
            super(env);
            this.annotations = ImmutableSet.of();
            this.rootElements = ImmutableSet.of();
            this.proxyElements = ImmutableSet.of();
            this.outputElements = ImmutableSet.of();
            this.inputRounds = ImmutableSet.of();
        }

        private Input(Input previous, Set<? extends TypeElement> annotations, Set<? extends Element> rootElements,
                Set<? extends ProxyElement> proxyElements, Set<? extends ProxyElement> outputElements,
                Set<ProxyElementRound> inputRounds) {
            super(previous);
            this.annotations = ImmutableSet.copyOf(annotations);
            this.rootElements = ImmutableSet.copyOf(rootElements);
            this.proxyElements = ImmutableSet.copyOf(proxyElements);
            this.outputElements = ImmutableSet.copyOf(outputElements);
            this.inputRounds = ImmutableSet.copyOf(inputRounds);
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

        public ImmutableSet<ProxyElementRound> inputRounds() {
            return inputRounds;
        }

        public ImmutableMap<ProxyElement, ImmutableSet<TypeElementOutput>> inputs() {
            ImmutableMap.Builder<ProxyElement, ImmutableSet<TypeElementOutput>> out
                    = ImmutableMap.builder();
            for (ProxyElementRound inputRound : inputRounds) {
                out.put(inputRound.element(), inputRound.outputs());
            }
            return out.build();
        }

        public ImmutableSet<ProxyElementOutput> allInputElements() {
            return buildSet(allRounds(), new Collector<ProxyElementOutput, Input>() {
                @Override public void collect(Input input, ImmutableCollection.Builder<ProxyElementOutput> out) {
                    for (ProxyElementRound inputRound : input.inputRounds) {
                        out.add(inputRound.elementOutput());
                    }
                }
            });
        }

        public ImmutableMap<ProxyElement, ImmutableSet<ProxyFunctionOutput>> allInputFunctions() {
            ImmutableMap.Builder<ProxyElement, ImmutableSet<ProxyFunctionOutput>> out
                    = ImmutableMap.builder();
            for (ProxyElementOutput elementOutput : allInputElements()) {
                out.put(elementOutput.element(), elementOutput.outputs());
            }
            return out.build();
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

        public ImmutableMap<ProxyElement, ImmutableSet<TypeElementOutput>> allInputs() {
            Input previous = previous();
            if (previous == null) {
                return inputs();
            }
            return combine(previous.allInputs(), inputs());
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
            final ImmutableSet<ProxyElementRound> inputRounds = lastOutput.nextInputRound(this);
            log("all proxy elements: %s", allProxyElements);

            final ImmutableSet<ProxyElement> unprocessedElements = unprocessed(allProxyElements);
            log("all unprocessed elements: %s", unprocessedElements);
            final Set<ProxyElement> processedElements = new HashSet<>(allOutputElements());
            for (ProxyElementRound inputRound : inputRounds) {
                if (!inputRound.isFinished()) {
                    processedElements.remove(inputRound.element());
                }
            }
            log("all processed elements: %s", processedElements);

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

            log("round: %s", round);

            return new Input(this, annotations, roundEnv.getRootElements(), curProxyElements, round, inputRounds);
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
        private final ImmutableSet<ProxyElementRound> outputRounds;

        private Output() {
            this.outputRounds = ImmutableSet.of();
        }

        private Output(Output previous, Set<ProxyElementRound> outputRounds) {
            super(previous);
            this.outputRounds = ImmutableSet.copyOf(outputRounds);
        }

        Output write(Input input) throws IOException {
            final ProxyEnv env = input.env();
            final Set<ProxyElementRound> outputRounds = new HashSet<>(input.inputRounds());

            for (ProxyElement proxyElement : input.outputElements()) {
                outputRounds.add(ProxyElementRound.create(proxyElement, env)
                        .nextRound(input));
            }

            return new Output(this, outputRounds);
        }

        public ImmutableSet<ProxyElementRound> outputRounds() {
            return outputRounds;
        }

        public ImmutableMap<ProxyElement, ImmutableSet<TypeElementOutput>> outputs() {
            ImmutableMap.Builder<ProxyElement, ImmutableSet<TypeElementOutput>> out
                    = ImmutableMap.builder();
            for (ProxyElementRound outputRound : outputRounds) {
                out.put(outputRound.element(), outputRound.outputs());
            }
            return out.build();
        }

        public ImmutableMap<ProxyElement, ImmutableSet<TypeElementOutput>> allOutputs() {
            Output previous = previous();
            if (previous == null) {
                return outputs();
            }
            return combine(previous.outputs(), outputs());
        }

        public ImmutableSet<ProxyElementRound> nextInputRound(final Input input) throws IOException {
            return buildSet(outputRounds, new Transformer<ProxyElementRound, ProxyElementRound>() {
                @Override public ProxyElementRound transform(ProxyElementRound proxyElementRound) {
                    if (!proxyElementRound.isFinished()) {
                        try {
                            return proxyElementRound.nextRound(input);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    return null;
                }
            });
        }

        public boolean didWrite() {
            for (Set<TypeElementOutput> set : outputs().values()) {
                for (TypeElementOutput output : set) {
                    if (output.didWrite()) {
                        return true;
                    }
                }
            }
            return false;
        }

        @Override public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("\noutputRounds", outputRounds)
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
