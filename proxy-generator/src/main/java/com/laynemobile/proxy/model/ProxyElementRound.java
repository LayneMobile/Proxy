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
import com.google.common.collect.ImmutableCollection.Builder;
import com.google.common.collect.ImmutableSet;
import com.laynemobile.proxy.Util.Collector;
import com.laynemobile.proxy.model.output.ProxyElementOutput;
import com.laynemobile.proxy.model.output.TypeElementOutputStub;

import java.io.IOException;

import sourcerer.processor.Env;

import static com.laynemobile.proxy.Util.buildSet;

public class ProxyElementRound extends EnvRound<ProxyElementRound> {
    private final ProxyElementOutput elementOutput;
    private final ImmutableSet<TypeElementOutputStub> outputStubs;

    private ProxyElementRound(Env env, ProxyElementOutput elementOutput) {
        super(env);
        this.elementOutput = elementOutput;
        this.outputStubs = ImmutableSet.of();
    }

    private ProxyElementRound(ProxyElementRound previous, ImmutableSet<TypeElementOutputStub> outputStubs) {
        super(previous);
        this.elementOutput = previous.elementOutput;
        this.outputStubs = outputStubs;
    }

    public static ProxyElementRound create(ProxyElement element, Env env) {
        ProxyElementOutput output = ProxyElementOutput.create(element);
        return new ProxyElementRound(env, output);
    }

    public ProxyElementOutput elementOutput() {
        return elementOutput;
    }

    public ProxyElement element() {
        return elementOutput.element();
    }

    public ImmutableSet<TypeElementOutputStub> outputStubs() {
        return outputStubs;
    }

    public ImmutableSet<TypeElementOutputStub> allOutputStubs() {
        return buildSet(allRounds(), new Collector<TypeElementOutputStub, ProxyElementRound>() {
            @Override public void collect(ProxyElementRound round, Builder<TypeElementOutputStub> out) {
                out.addAll(round.outputStubs());
            }
        });
    }

    public boolean isFinished() {
        return elementOutput.isFinished();
    }

    public ProxyElementRound nextRound(ProxyRound.Input input) throws IOException {
        return new ProxyElementRound(this, elementOutput.nextOutputStubs(input));
    }

    @Override public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("\nelementOutput", elementOutput)
                .add("\noutputStubs", outputStubs)
                .toString();
    }
}
