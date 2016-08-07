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

import com.laynemobile.api.NoParams;
import com.laynemobile.api.SimpleSource;
import com.laynemobile.api.SourceHandlerBuilder;
import com.laynemobile.api.functions.SimpleSource_call__NoParams_Subscriber;
import com.laynemobile.proxy.AbstractProxyHandlerBuilder;
import com.laynemobile.proxy.annotations.Generated;
import com.laynemobile.proxy.functions.Action1;
import com.laynemobile.proxy.functions.Func0;

import rx.Observable;
import rx.Subscriber;

@Generated
public abstract class SimpleSourceHandlerBuilder<T> extends AbstractProxyHandlerBuilder<SimpleSource<T>> {
    private final SourceHandlerBuilder<T, NoParams> source = new SourceHandlerBuilder<>();

    public SimpleSourceHandlerBuilder<T> setSource(SimpleSource_call__NoParams_Subscriber<? extends T> source) {
        this.source.setSource(source);
        return this;
    }

    public SimpleSourceHandlerBuilder<T> setSource(Action1<Subscriber<? super T>> source) {
        this.source.setSource(new SimpleSource_call__NoParams_Subscriber<T>(source));
        return this;
    }

    public SimpleSourceHandlerBuilder<T> setSource(Func0<T> source) {
        this.source.setSource(new SimpleSource_call__NoParams_Subscriber<T>(source));
        return this;
    }

    public SimpleSourceHandlerBuilder<T> setSource(Observable<T> source) {
        this.source.setSource(new SimpleSource_call__NoParams_Subscriber<T>(source));
        return this;
    }

    // TODO: need to be able to add super type, user super builder
//    @Override
//    public ProxyHandler<SimpleSource<T>> build() {
//        return ProxyHandler.builder(new TypeToken<SimpleSource<T>>() {})
//                .handle(handler(source))
//                .build();
//    }
}
