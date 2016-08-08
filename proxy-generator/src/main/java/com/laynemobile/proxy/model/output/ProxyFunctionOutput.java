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

package com.laynemobile.proxy.model.output;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.laynemobile.proxy.model.AnnotatedProxyElement;
import com.laynemobile.proxy.model.ProxyElement;
import com.laynemobile.proxy.model.ProxyEnv;
import com.laynemobile.proxy.model.ProxyFunctionElement;
import com.laynemobile.proxy.model.ProxyRound;
import com.laynemobile.proxy.model.ProxyType;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

import static com.laynemobile.proxy.Util.typeMirrorArray;

public class ProxyFunctionOutput {
    private final AnnotatedProxyElement parent;
    private final ProxyFunctionElement element;
    private ProxyFunctionAbstractTypeOutputStub abstractTypeOutputStub;
    private ProxyFunctionAbstractTypeOutput abstractTypeOutput;
    private ProxyFunctionTypeOutputStub typeOutputStub;
    private TypeElementOutput typeOutput;
    private boolean finished;

    ProxyFunctionOutput(AnnotatedProxyElement parent, ProxyFunctionElement element) {
        this.parent = parent;
        this.element = element;
    }

    public AnnotatedProxyElement parent() {
        return parent;
    }

    public ProxyFunctionElement element() {
        return element;
    }

    public ProxyFunctionAbstractTypeOutputStub abstractTypeOutputStub() {
        return abstractTypeOutputStub;
    }

    public ProxyFunctionAbstractTypeOutput abstractTypeOutput() {
        return abstractTypeOutput;
    }

    public ProxyFunctionTypeOutputStub typeOutputStub() {
        return typeOutputStub;
    }

    public TypeElementOutput typeOutput() {
        return typeOutput;
    }

    public synchronized boolean isFinished() {
        return finished;
    }

    public synchronized TypeElementOutput nextOutput(ProxyRound.Input input)
            throws IOException {
        ProxyEnv env = input.env();
        TypeElementOutput output = null;
        if (abstractTypeOutputStub == null) {
            TypeElementOutputStub stub = firstOutputStub(input, element.outputStub());
            if (stub instanceof ProxyFunctionTypeOutputStub) {
                finished = true;
                // skip abstract type
                typeOutputStub = (ProxyFunctionTypeOutputStub) stub;
                output = typeOutput = typeOutput(input, typeOutputStub);
            } else {
                abstractTypeOutputStub = (ProxyFunctionAbstractTypeOutputStub) stub;
                output = abstractTypeOutput = abstractTypeOutputStub.writeTo(env);
            }
        } else if (typeOutputStub == null) {
            finished = true;
            if (abstractTypeOutput.hasOutput()) {
                typeOutputStub = abstractTypeOutput.outputStub(env);
                output = typeOutput = typeOutput(input, typeOutputStub);
            }
        }
        return output;
    }

    private static TypeElementOutput typeOutput(ProxyRound.Input input, ProxyFunctionTypeOutputStub typeOutputStub)
            throws IOException {
        String typeOutputName = typeOutputStub.qualifiedName();
        for (Element rootElement : input.allRootElements()) {
            if (rootElement.getKind() != ElementKind.CLASS) {
                continue;
            }

            TypeElement typeElement = (TypeElement) rootElement;
            if (typeOutputName.equals(typeElement.getQualifiedName().toString())) {
                // already created
                return AbstractTypeElementOutput.existing(typeOutputStub);
            }
        }
        return typeOutputStub.writeTo(input.env());
    }

    private TypeElementOutputStub firstOutputStub(ProxyRound.Input input,
            ProxyFunctionAbstractTypeOutputStub outputStub) {
        final ProxyEnv env = input.env();
        final AnnotatedProxyElement parent = this.parent;
        final ImmutableMap<AnnotatedProxyElement, ImmutableSet<ProxyFunctionOutput>> inputFunctions = input.allInputFunctions();
        for (ProxyFunctionElement override : element.overrides()) {
            ProxyElement overrideParentElement = override.parent();
            Set<ProxyFunctionOutput> set = null;
            for (Map.Entry<AnnotatedProxyElement, ImmutableSet<ProxyFunctionOutput>> entry : inputFunctions.entrySet()) {
                if (overrideParentElement.equals(entry.getKey().element())) {
                    set = entry.getValue();
                    break;
                }
            }
            if (set == null) {
                continue;
            }
            for (ProxyFunctionOutput functionOutput : set) {
                ProxyFunctionTypeOutputStub generated = functionOutput.typeOutputStub;
                env.log("say man");
                env.log("%s -- writing override '%s' from '%s' -- %s", parent.toDebugString(),
                        outputStub.qualifiedName(), override, generated);
                env.log("say man");

                ProxyType overrideParentType = null;
                for (ProxyType test : parent.element().directDependencies()) {
                    if (test.element().equals(overrideParentElement)) {
                        overrideParentType = test;
                        break;
                    }
                }
                if (overrideParentType == null) {
                    continue;
                }

                TypeMirror[] typeParams = typeMirrorArray(overrideParentType.type().actual().getTypeArguments());
                final TypeElement superElement = generated.element(env);
                env.log("super proxy element '%s', type parameters: '%s'", superElement,
                        Arrays.toString(typeParams));
                if (superElement == null) {
                    continue;
                }
                DeclaredType superType = env.types()
                        .getDeclaredType(superElement, typeParams);
                env.log("super proxy type '%s'", superType);
                return new ProxyFunctionTypeOutputStub(outputStub, superType, env);
            }
        }
        return outputStub;
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProxyFunctionOutput)) return false;
        ProxyFunctionOutput that = (ProxyFunctionOutput) o;
        return Objects.equal(element, that.element);
    }

    @Override public int hashCode() {
        return Objects.hashCode(element);
    }
}
