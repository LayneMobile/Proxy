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

package com.laynemobile.api;

import com.google.common.base.Objects;
import com.laynemobile.proxy.ConsoleLogger;
import com.laynemobile.proxy.ProxyCompleter;
import com.laynemobile.proxy.functions.Func0;
import com.laynemobile.proxy.functions.Func1;
import com.laynemobile.proxy.internal.ProxyLog;

import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import rx.Subscriber;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class Tester {
    static {
        ProxyLog.setLogger(new ConsoleLogger());
    }

    @Test public void testSourceProxy() throws Throwable {
        final ProxyCompleter<Source<Potato, PotatoParams>> sourceCompleter;
        Source<Potato, PotatoParams> source;
        sourceCompleter = new SourceProxyHandlerBuilder2<Potato, PotatoParams>()
                .setSource(new Func1<PotatoParams, Potato>() {
                    @Override public Potato call(PotatoParams params) {
                        return new Potato(params.kind());
                    }
                });
        source = sourceCompleter.build();

        final PotatoParams params = new DefaultPotatoParams("russet");
        runPotatoSourceTest(source, params);
    }

    @Test
    public void testAggregableProxy() throws Exception {
        final Object key = new Object();
        Aggregable aggregable = new AggregableProxyHandlerBuilder2()
                .setKey(new Func0<Object>() {
                    @Override public Object call() {
                        return key;
                    }
                })
                .setKeepAliveSeconds(new Func1<AggregableProxyHandlerBuilder2.State1, Integer>() {
                    @Override public Integer call(AggregableProxyHandlerBuilder2.State1 state1) {
                        Object key = state1.key.function.call();
                        return key == null ? 1 : 0;
                    }
                })
                .setKeepAliveOnError(new Func1<AggregableProxyHandlerBuilder2.State2, Boolean>() {
                    @Override public Boolean call(AggregableProxyHandlerBuilder2.State2 state2) {
                        Object key = state2.key.function.call();
                        int keepAliveSeconds = state2.keepAliveSeconds.function.call();
                        return keepAliveSeconds > 0;
                    }
                })
                .build();
        assertEquals(key, aggregable.key());
        assertEquals(0, aggregable.keepAliveSeconds());
        assertFalse(aggregable.keepAliveOnError());
    }

    private static void runPotatoSourceTest(Source<Potato, PotatoParams> source, PotatoParams params)
            throws Throwable {
        final AtomicReference<Potato> onNext = new AtomicReference<>();
        final AtomicBoolean onCompleted = new AtomicBoolean();
        final AtomicReference<Throwable> onError = new AtomicReference<>();

        final CountDownLatch latch = new CountDownLatch(1);
        source.call(params, new Subscriber<Potato>() {
            @Override public void onNext(Potato potato) {
                onNext.set(potato);
                latch.countDown();
            }

            @Override public void onCompleted() {
                onCompleted.set(true);
                latch.countDown();
            }

            @Override public void onError(Throwable e) {
                onError.set(e);
                latch.countDown();
            }
        });
        latch.await();

        Throwable e = onError.get();
        if (e != null) {
            throw e;
        }

        assertEquals(new Potato(params.kind()), onNext.get());
        assertTrue(onCompleted.get());
        assertNull(onError.get());
    }

    private static final class Potato {
        private final String kind;

        private Potato(String kind) {
            this.kind = kind;
        }

        private String kind() {
            return kind;
        }

        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Potato)) return false;
            Potato potato = (Potato) o;
            return Objects.equal(kind, potato.kind);
        }

        @Override public int hashCode() {
            return Objects.hashCode(kind);
        }
    }

    private interface PotatoParams extends Params {
        String kind();
    }

    private static final class DefaultPotatoParams implements PotatoParams {
        private final String kind;

        private DefaultPotatoParams(String kind) {
            this.kind = kind;
        }

        @Override public String kind() {
            return kind;
        }

        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof DefaultPotatoParams)) return false;
            DefaultPotatoParams that = (DefaultPotatoParams) o;
            return Objects.equal(kind, that.kind);
        }

        @Override public int hashCode() {
            return Objects.hashCode(kind);
        }
    }

    private interface PotatoFactory {
        Potato newPotato(PotatoParams params);
    }

    private static final class DefaultPotatoFactory implements PotatoFactory, Retrofittable<PotatoFactory> {
        @Override public Potato newPotato(PotatoParams params) {
            return new Potato(params.kind());
        }

        @Override public PotatoFactory getService() {
            return this;
        }
    }
}
