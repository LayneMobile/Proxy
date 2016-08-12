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

package com.laynemobile;

import com.google.common.base.MoreObjects;
import com.laynemobile.api.NetworkChecker;
import com.laynemobile.api.NetworkSource;
import com.laynemobile.api.NetworkSourceProxyHandlerBuilder;
import com.laynemobile.api.NoParams;
import com.laynemobile.api.SimpleSource;
import com.laynemobile.api.SimpleSourceProxyHandlerBuilder2;
import com.laynemobile.api.Source;
import com.laynemobile.proxy.ConsoleLogger;
import com.laynemobile.proxy.ProxyBuilder;
import com.laynemobile.proxy.ProxyHandler;
import com.laynemobile.proxy.TypeToken;
import com.laynemobile.proxy.functions.Func0;
import com.laynemobile.proxy.internal.ProxyLog;

import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import rx.Subscriber;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class Tester {
    static {
        ProxyLog.setLogger(new ConsoleLogger());
    }

    @Test public void testSourceProxy() throws Throwable {
        final TypeToken<Source<Potato, NoParams>> type = new TypeToken<Source<Potato, NoParams>>() {};
        final Potato potato = new Potato("russet");
        final NetworkChecker networkChecker = new SimpleNetworkChecker();

        // Create simple source handler
        ProxyHandler<SimpleSource<Potato>> sourceHandler = new SimpleSourceProxyHandlerBuilder2<Potato>()
                .setSource(new Func0<Potato>() {
                    @Override public Potato call() {
                        return potato;
                    }
                })
                .build();

        // Create network source handler
        ProxyHandler<NetworkSource<Potato, NoParams>> networkSourceHandler = new NetworkSourceProxyHandlerBuilder<Potato, NoParams>()
                .setNetworkChecker(networkChecker)
                .build();

        // Create the proxy
        Source<Potato, NoParams> _source = new ProxyBuilder<>(type)
                .add(sourceHandler)
                .add(networkSourceHandler)
                .build();

        // Note: able to cast by adding SimpleSource proxy handler
        SimpleSource<Potato> simpleSource = (SimpleSource<Potato>) _source;
        // Note: able to cast by adding NetworkSource proxy handler
        NetworkSource<Potato, NoParams> networkSource = (NetworkSource<Potato, NoParams>) _source;

        final AtomicReference<Potato> onNext = new AtomicReference<>();
        final AtomicBoolean onCompleted = new AtomicBoolean();
        final AtomicReference<Throwable> onError = new AtomicReference<>();

        final CountDownLatch latch = new CountDownLatch(1);
        simpleSource.call(NoParams.instance(), new Subscriber<Potato>() {
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

        assertEquals(potato, onNext.get());
        assertTrue(onCompleted.get());
        assertNull(onError.get());
        assertEquals(networkChecker, networkSource.networkChecker());
    }

    private static final class Potato {
        private final String kind;

        private Potato(String kind) {
            this.kind = kind;
        }

        private String kind() {
            return kind;
        }
    }

    private static final class SimpleNetworkChecker implements NetworkChecker {
        final boolean networkAvailable;

        SimpleNetworkChecker() {
            this(true);
        }

        SimpleNetworkChecker(boolean networkAvailable) {
            this.networkAvailable = networkAvailable;
        }

        @Override public boolean isNetworkAvailable() {
            return networkAvailable;
        }

        @Override public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("networkAvailable", networkAvailable)
                    .toString();
        }
    }
}
