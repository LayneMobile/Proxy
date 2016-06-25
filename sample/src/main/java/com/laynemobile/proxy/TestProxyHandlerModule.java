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

import com.laynemobile.proxy.annotations.ProxyHandlerModule;
import com.laynemobile.proxy.internal.ProxyLog;
import com.laynemobile.proxy.processor.ErrorHandler;
import com.laynemobile.proxy.processor.Processor;
import com.laynemobile.proxy.processor.ProcessorHandler;

import java.lang.reflect.Method;

import rx.functions.Func1;

@ProxyHandlerModule
public class TestProxyHandlerModule implements Builder<ProcessorHandler.Parent<Integer, String, TestInterface>> {
    private Func1<Integer, String> source;

    public void setSource(Func1<Integer, String> source) {
        this.source = source;
    }

    @Override public ProcessorHandler.Parent<Integer, String, TestInterface> build() {
        return build(ProxyHandler.builder(TestInterface.class)
                .handle("transform", new Handler(source))
                .build());
    }

    private static ProcessorHandler.Parent<Integer, String, TestInterface> build(
            final ProxyHandler<TestInterface> proxyHandler) {
        return new ProcessorHandler.Parent<Integer, String, TestInterface>() {
            @Override public ProxyHandler<TestInterface> proxyHandler() {
                return proxyHandler;
            }

            @Override public Processor.Parent<Integer, String> extension(final TestInterface testInterface) {
                return new Processor.Parent<Integer, String>() {
                    @Override public ErrorHandler<String> errorHandler() {
                        return new ErrorHandler<String>() {
                            @Override public String onError(Throwable throwable) {
                                return ProxyLog.getStackTraceString(throwable);
                            }
                        };
                    }

                    @Override public String call(Integer integer) {
                        return testInterface.transform(integer);
                    }
                };
            }
        };
    }

    private static final class Handler implements MethodHandler {
        private final Func1<Integer, String> func;

        private Handler(Func1<Integer, String> func) {
            this.func = func;
        }

        @Override
        public boolean handle(Object proxy, Method method, Object[] args, MethodResult result) throws Throwable {
            Class<?>[] paramTypes = method.getParameterTypes();
            if (paramTypes.length == 1
                    && paramTypes[0].isAssignableFrom(Integer.class)) {
                String value = func.call((Integer) args[0]);
                result.set(value);
                return true;
            }
            return false;
        }
    }
}
