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
import com.laynemobile.proxy.model.ProxyElement;
import com.laynemobile.proxy.model.ProxyFunctionElement;
import com.laynemobile.proxy.model.ProxyType;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

import sourcerer.processor.Env;

import static com.laynemobile.proxy.Util.toArray;

public class ProxyFunctionOutput {
    private final ProxyElement parent;
    private final ProxyFunctionElement element;
    private ProxyFunctionAbstractTypeOutputStub abstractTypeOutputStub;
    private ProxyFunctionAbstractTypeOutput abstractTypeOutput;
    private ProxyFunctionTypeOutputStub typeOutputStub;
    private TypeElementOutput typeOutput;

    ProxyFunctionOutput(ProxyElement parent, ProxyFunctionElement element) {
        this.parent = parent;
        this.element = element;
    }

    public ProxyElement parent() {
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
        return typeOutput != null;
    }

    public synchronized TypeElementOutputStub nextOutputStub(
            Map<ProxyElement, ? extends Set<TypeElementOutputStub>> inputs,
            Env env) throws IOException {
        if (abstractTypeOutputStub == null) {
            return firstOutputStub(inputs, env);
        } else if (abstractTypeOutput == null) {
            abstractTypeOutput = abstractTypeOutputStub.writeTo(env);
            if (abstractTypeOutput.hasOutput()) {
                return typeOutputStub = abstractTypeOutput.outputStub(env);
            }
        } else if (typeOutputStub == null) {
            return typeOutputStub = abstractTypeOutput.outputStub(env);
        } else if (typeOutput == null) {
            typeOutput = typeOutputStub.writeTo(env);
            if (typeOutput.hasOutput()) {
                return typeOutput.outputStub(env);
            }
        }
        return null;
    }

    private TypeElementOutputStub firstOutputStub(
            Map<ProxyElement, ? extends Set<TypeElementOutputStub>> inputs,
            Env env) {
        final ProxyElement parent = this.parent;
        final ProxyFunctionAbstractTypeOutputStub outputStub = element.outputStub();
        for (ProxyFunctionElement override : element.overrides()) {
            ProxyElement overrideParentElement = override.parent();
            Set<TypeElementOutputStub> set = inputs.get(overrideParentElement);
            if (set == null) {
                continue;
            }
            for (TypeElementOutputStub generated : set) {
                env.log("say man");
                env.log("%s -- writing override '%s' from '%s' -- %s", parent.toDebugString(),
                        outputStub.qualifiedName(), override, generated);
                env.log("say man");

                ProxyType overrideParentType = null;
                for (ProxyType test : parent.directDependencies()) {
                    if (test.element().equals(overrideParentElement)) {
                        overrideParentType = test;
                        break;
                    }
                }
                if (overrideParentType == null) {
                    continue;
                }

                TypeMirror[] typeParams = toArray(overrideParentType.type().actual().getTypeArguments());
                final TypeElement superElement = generated.element(env);
                env.log("super proxy element '%s', type parameters: '%s'", superElement,
                        Arrays.toString(typeParams));
                if (superElement == null) {
                    continue;
                }
                DeclaredType superType = env.types()
                        .getDeclaredType(superElement, typeParams);
                return outputStub.withSuperClass(superType);
            }
        }
        return abstractTypeOutputStub = outputStub;
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
