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

import com.laynemobile.api.SimpleParams;
import com.laynemobile.proxy.functions.Action1;
import com.laynemobile.proxy.functions.Func0;

import rx.Observable;
import rx.Subscriber;

// subclass template created for user for constructor extensions
public class SimpleSource_callFunction<T> extends Source_callFunction<T, SimpleParams> {
    public SimpleSource_callFunction(Action1<Subscriber<? super T>> action1) {
        super(action1);
    }

    public SimpleSource_callFunction(Func0<T> func0) {
        super(func0);
    }

    public SimpleSource_callFunction(Observable<T> observable) {
        super(observable);
    }
}
