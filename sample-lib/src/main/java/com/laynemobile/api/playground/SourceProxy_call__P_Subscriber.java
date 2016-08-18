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

package com.laynemobile.api.playground;

import com.laynemobile.api.Params;
import com.laynemobile.proxy.annotations.Generated;
import com.laynemobile.proxy.functions.Action1;
import com.laynemobile.proxy.functions.Action2;
import com.laynemobile.proxy.functions.Func0;
import com.laynemobile.proxy.functions.Func1;

import rx.Observable;
import rx.Subscriber;

@Generated
public class SourceProxy_call__P_Subscriber<T, P extends Params> extends AbstractSourceProxy_call__P_Subscriber<T, P> {
    public SourceProxy_call__P_Subscriber(SourceDef_call__P_Subscriber<? extends T, ? extends P> functionDef,
            Action2<P, Subscriber<? super T>> action) {
        super(functionDef, action);
    }

    public SourceProxy_call__P_Subscriber(Action2<P, Subscriber<? super T>> source) {
        super(source);
    }

    public SourceProxy_call__P_Subscriber(final Action1<Subscriber<? super T>> source) {
        super(new Action2<P, Subscriber<? super T>>() {
            @Override public void call(P p, Subscriber<? super T> subscriber) {
                source.call(subscriber);
            }
        });
    }

    public SourceProxy_call__P_Subscriber(final Func1<P, T> source) {
        super(new Action2<P, Subscriber<? super T>>() {
            @Override public void call(P p, Subscriber<? super T> subscriber) {
                try {
                    T t = source.call(p);
                    subscriber.onNext(t);
                    subscriber.onCompleted();
                } catch (Throwable e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    public SourceProxy_call__P_Subscriber(final Func0<T> source) {
        this(new Func1<P, T>() {
            @Override public T call(P p) {
                return source.call();
            }
        });
    }

    public SourceProxy_call__P_Subscriber(final Observable<T> source) {
        super(new Action2<P, Subscriber<? super T>>() {
            @Override public void call(P p, final Subscriber<? super T> child) {
                source.unsafeSubscribe(new Subscriber<T>(child) {
                    @Override public void onCompleted() {
                        if (!child.isUnsubscribed()) {
                            child.onCompleted();
                        }
                    }

                    @Override public void onError(Throwable e) {
                        if (!child.isUnsubscribed()) {
                            child.onError(e);
                        }
                    }

                    @Override public void onNext(T t) {
                        if (!child.isUnsubscribed()) {
                            child.onNext(t);
                        }
                    }
                });
            }
        });
    }
}
