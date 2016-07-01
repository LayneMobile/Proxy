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

package com.laynemobile.api.templates;

import com.laynemobile.api.Params;
import com.laynemobile.api.generated.AbstractSource_callFunction;
import com.laynemobile.proxy.functions.Action1;
import com.laynemobile.proxy.functions.Action2;
import com.laynemobile.proxy.functions.Func0;
import com.laynemobile.proxy.functions.Func1;

import rx.Observable;
import rx.Subscriber;

// subclass template created for user for constructor extensions
public class Source_callFunction<T, P extends Params> extends AbstractSource_callFunction<T, P> {
    public Source_callFunction(Action2<P, Subscriber<? super T>> action2) {
        super(action2);
    }

    public Source_callFunction(final Action1<Subscriber<? super T>> action1) {
        super(new Action2<P, Subscriber<? super T>>() {
            @Override public void call(P p, Subscriber<? super T> subscriber) {
                action1.call(subscriber);
            }
        });
    }

    public Source_callFunction(final Func1<P, T> func1) {
        super(new Action2<P, Subscriber<? super T>>() {
            @Override public void call(P p, Subscriber<? super T> subscriber) {
                try {
                    T t = func1.call(p);
                    subscriber.onNext(t);
                    subscriber.onCompleted();
                } catch (Throwable e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    public Source_callFunction(final Func0<T> func0) {
        this(new Func1<P, T>() {
            @Override public T call(P p) {
                return func0.call();
            }
        });
    }

    public Source_callFunction(final Observable<T> observable) {
        super(new Action2<P, Subscriber<? super T>>() {
            @Override public void call(P p, final Subscriber<? super T> child) {
                observable.unsafeSubscribe(new Subscriber<T>(child) {
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
