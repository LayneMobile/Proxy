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

package com.laynemobile.proxy.functions.transforms;

import com.laynemobile.proxy.functions.Action0;
import com.laynemobile.proxy.functions.Action1;
import com.laynemobile.proxy.functions.Actions;

public class Action1Transform<T>
        extends ActionTransform<Action1<? super T>>
        implements Action1<T> {

    public Action1Transform() {
        super(Actions.empty());
    }

    public Action1Transform(Action1<? super T> action) {
        super(action);
    }

    public Action1Transform(Action1Transform<? super T> action) {
        super(action.function);
    }

    public Action1Transform(final Action0 action) {
        super(new Action1<T>() {
            @Override public void call(T t) {
                action.call();
            }
        });
    }

    @SuppressWarnings("unchecked")
    @Override protected final void invoke(Object... args) {
        if (args.length != 1) {
            throw new RuntimeException("Action1 expecting 1 arguments.");
        }
        function.call((T) args[0]);
    }

    @Override public final void call(T t) {
        function.call(t);
    }
}
