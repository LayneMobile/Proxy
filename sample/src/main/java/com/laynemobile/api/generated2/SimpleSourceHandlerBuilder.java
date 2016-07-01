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

package com.laynemobile.api.generated2;

import com.laynemobile.api.SimpleSource;
import com.laynemobile.api.templates.SimpleSource_callFunction;
import com.laynemobile.proxy.Builder;
import com.laynemobile.proxy.NamedMethodHandler;
import com.laynemobile.proxy.ProxyHandler;
import com.laynemobile.proxy.TypeToken;
import com.laynemobile.proxy.functions.Action1;
import com.laynemobile.proxy.functions.Func0;

import rx.Observable;
import rx.Subscriber;

// Generated builder (2nd pass)
// @Replaces(Source.class)
public class SimpleSourceHandlerBuilder<T> implements Builder<ProxyHandler<SimpleSource<T>>> {
    private SimpleSource_callFunction<T> source;

    public SimpleSourceHandlerBuilder<T> source(Action1<Subscriber<? super T>> source) {
        this.source = new SimpleSource_callFunction<>(source);
        return this;
    }

    public SimpleSourceHandlerBuilder<T> source(Func0<T> source) {
        this.source = new SimpleSource_callFunction<>(source);
        return this;
    }

    public SimpleSourceHandlerBuilder<T> source(Observable<T> source) {
        this.source = new SimpleSource_callFunction<>(source);
        return this;
    }

    @Override public ProxyHandler<SimpleSource<T>> build() {
        final SimpleSource_callFunction<T> source = this.source;
        if (source == null) {
            throw new IllegalStateException("source function must be set");
        }
        final NamedMethodHandler handler = source.handler();
        if (handler == null) {
            throw new IllegalStateException("source function handler must not be null");
        }
        return ProxyHandler.builder(new TypeToken<SimpleSource<T>>() {})
                .handle(handler)
                .build();
    }
}
