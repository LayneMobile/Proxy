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
import com.google.common.collect.ImmutableSet;
import com.laynemobile.proxy.Util.Transformer;
import com.laynemobile.proxy.model.ProxyElement;
import com.laynemobile.proxy.model.ProxyEnv;
import com.laynemobile.proxy.model.ProxyFunctionElement;
import com.laynemobile.proxy.model.ProxyRound;

import java.io.IOException;

import static com.laynemobile.proxy.Util.buildSet;

public class ProxyElementOutput {
    private final ProxyElement element;
    private final ImmutableSet<ProxyFunctionOutput> outputs;
    private ProxyHandlerBuilderOutputStub handlerBuilderOutputStub;

    private ProxyElementOutput(final ProxyElement element) {
        this.element = element;
        this.outputs = buildSet(element.functions(), new Transformer<ProxyFunctionOutput, ProxyFunctionElement>() {
            @Override public ProxyFunctionOutput transform(ProxyFunctionElement proxyFunctionElement) {
                return new ProxyFunctionOutput(element, proxyFunctionElement);
            }
        });
    }

    public static ProxyElementOutput create(ProxyElement element) {
        return new ProxyElementOutput(element);
    }

    public ProxyElement element() {
        return element;
    }

    public boolean isFinished() {
        for (ProxyFunctionOutput output : outputs) {
            if (!output.isFinished()) {
                return false;
            }
        }
        synchronized (this) {
            return handlerBuilderOutputStub != null;
        }
    }

    public synchronized ImmutableSet<TypeElementOutputStub> nextOutputStubs(ProxyRound.Input input)
            throws IOException {
        ProxyEnv env = input.env();
        boolean functionsFinished = true;
        ImmutableSet.Builder<TypeElementOutputStub> out = ImmutableSet.builder();
        for (ProxyFunctionOutput functionOutput : outputs) {
            if (!functionOutput.isFinished()) {
                functionsFinished = false;
                TypeElementOutputStub outputStub = functionOutput.nextOutputStub(input);
                if (outputStub != null) {
                    out.add(outputStub);
                }
            }
        }
        if (functionsFinished) {
            if (handlerBuilderOutputStub == null) {
                out.add(handlerBuilderOutputStub = ProxyHandlerBuilderOutputStub.create(env, element, outputs));
            }
        }
        return out.build();
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProxyElementOutput)) return false;
        ProxyElementOutput that = (ProxyElementOutput) o;
        return Objects.equal(element, that.element);
    }

    @Override public int hashCode() {
        return Objects.hashCode(element);
    }
}
