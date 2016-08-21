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

import com.laynemobile.proxy.functions.Func0;
import com.laynemobile.proxy.functions.Func1;

public class Func1Transform<T, R>
        extends FunctionTransform<Func1<? super T, ? extends R>, R>
        implements Func1<T, R> {

    public Func1Transform(Func1<? super T, ? extends R> function) {
        super(function);
    }

    public Func1Transform(Func1Transform<? super T, ? extends R> function) {
        super(function.function);
    }

    public Func1Transform(final Func0<? extends R> function) {
        super(new Func1<T, R>() {
            @Override public R call(T t) {
                return function.call();
            }
        });
    }

    public Func1Transform(final R value) {
        super(new Func1<T, R>() {
            @Override public R call(T t) {
                return value;
            }
        });
    }

    @Override public final R call(T t) {
        return function.call(t);
    }

    @SuppressWarnings("unchecked")
    @Override public final R call(Object... args) {
        if (args.length != 1) {
            throw new RuntimeException("Func1 expecting 1 arguments.");
        }
        return function.call((T) args[0]);
    }
}
