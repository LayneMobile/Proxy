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

package com.laynemobile.proxy;

import com.laynemobile.proxy.functions.Func1;
import com.laynemobile.proxy.processor.Processor;
import com.laynemobile.proxy.processor.ProcessorBuilder;
import com.laynemobile.proxy.processor.ProcessorHandler;

//@GenerateProxyBuilder(TestProxyHandlerModule.class)
public class TestApi {

    public Step2 setSource(Func1<Integer, String> source) {
        TestInterfaceProcessorHandlerBuilder<Integer, String> builder = new TestInterfaceProcessorHandlerBuilder<>();
        builder.setSource(source);
        return new Step2(builder.build());
    }

    public static final class Step2 {
        private final ProcessorBuilder<Integer, String, TestInterface<Integer, String>> builder;

        private Step2(ProcessorHandler.Parent<Integer, String, TestInterface<Integer, String>> parent) {
            this.builder = ProcessorBuilder.create(parent);
        }

        public Processor<Integer, String> build() {
            return builder.build();
        }
    }
}
