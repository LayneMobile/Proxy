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

import com.laynemobile.api.Params;
import com.laynemobile.api.Source;
import com.laynemobile.api.templates.Source_callFunction;
import com.laynemobile.proxy.Builder;
import com.laynemobile.proxy.NamedMethodHandler;
import com.laynemobile.proxy.ProxyHandler;
import com.laynemobile.proxy.TypeToken;
import com.laynemobile.proxy.functions.Action1;
import com.laynemobile.proxy.functions.Action2;
import com.laynemobile.proxy.functions.Func0;
import com.laynemobile.proxy.functions.Func1;

import rx.Observable;
import rx.Subscriber;

// Generated builder (2nd pass)
// @Parent
public class SourceHandlerBuilder<T, P extends Params> implements Builder<ProxyHandler<Source<T, P>>> {
    private Source_callFunction<T, P> source;

    public SourceHandlerBuilder<T, P> source(Action2<P, Subscriber<? super T>> source) {
        this.source = new Source_callFunction<>(source);
        return this;
    }

    public SourceHandlerBuilder<T, P> source(Action1<Subscriber<? super T>> source) {
        this.source = new Source_callFunction<>(source);
        return this;
    }

    public SourceHandlerBuilder<T, P> source(Func1<P, T> source) {
        this.source = new Source_callFunction<>(source);
        return this;
    }

    public SourceHandlerBuilder<T, P> source(Func0<T> source) {
        this.source = new Source_callFunction<>(source);
        return this;
    }

    public SourceHandlerBuilder<T, P> source(Observable<T> source) {
        this.source = new Source_callFunction<>(source);
        return this;
    }

    @Override public ProxyHandler<Source<T, P>> build() {
        final Source_callFunction<T, P> source = this.source;
        if (source == null) {
            throw new IllegalStateException("source function must be set");
        }
        final NamedMethodHandler handler = source.handler();
        if (handler == null) {
            throw new IllegalStateException("source function handler must not be null");
        }
        return ProxyHandler.builder(new TypeToken<Source<T, P>>() {})
                .handle(handler)
                .build();
    }
}
