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

import com.laynemobile.api.NoParams;
import com.laynemobile.proxy.functions.Action0;
import com.laynemobile.proxy.functions.Action1;
import com.laynemobile.proxy.functions.Action2;
import com.laynemobile.proxy.functions.Func0;
import com.laynemobile.proxy.functions.Func1;

import rx.Observable;
import rx.Subscriber;

public class SimpleSource_call__NoParams_Subscriber<T> extends Source_call__P_Subscriber<T, NoParams> {

    public SimpleSource_call__NoParams_Subscriber(SimpleSource_call__NoParams_Subscriber<T> proxyFunction) {
        super(proxyFunction);
    }

    public SimpleSource_call__NoParams_Subscriber(Def<T> functionDef, Transform<T> action) {
        super(functionDef, action);
    }

    public SimpleSource_call__NoParams_Subscriber(Transform<T> action) {
        super(new Def<T>(), action);
    }

    public static class Def<T> extends Source_call__P_Subscriber.Def<T, NoParams> {
        public Def() {
            super(new ParamTypes<NoParams, Subscriber<? super T>>() {});
        }

        public Def(ParamTypes<NoParams, Subscriber<? super T>> types) {
            super(types);
        }
    }

    public static class Transform<T> extends SourceTransform_call__P_Subscriber<T, NoParams> {
        public Transform(Action2<? super NoParams, ? super Subscriber<? super T>> action) {
            super(action);
        }

        public Transform(Action0 action) {
            super(action);
        }

        public Transform(Action1<? super Subscriber<? super T>> source) {
            super(source);
        }

        public Transform(Func1<? super NoParams, ? extends T> source) {
            super(source);
        }

        public Transform(Func0<? extends T> source) {
            super(source);
        }

        public Transform(Observable<? extends T> source) {
            super(source);
        }
    }
}
