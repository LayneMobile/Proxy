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
import com.laynemobile.proxy.functions.Func2;

public class Func2Transform<T1, T2, R>
        extends FunctionTransform<Func2<? super T1, ? super T2, ? extends R>, R>
        implements Func2<T1, T2, R> {

    public Func2Transform(Func2<? super T1, ? super T2, ? extends R> function) {
        super(function);
    }

    public Func2Transform(final Func0<? extends R> function) {
        super(new Func2<T1, T2, R>() {
            @Override public R call(T1 t1, T2 t2) {
                return function.call();
            }
        });
    }

    public Func2Transform(final R value) {
        super(new Func2<T1, T2, R>() {
            @Override public R call(T1 t1, T2 t2) {
                return value;
            }
        });
    }

    @Override public final R call(T1 t1, T2 t2) {
        return function.call(t1, t2);
    }

    @SuppressWarnings("unchecked")
    @Override public final R call(Object... args) {
        if (args.length != 2) {
            throw new RuntimeException("Func2 expecting 2 arguments.");
        }
        return function.call((T1) args[0], (T2) args[1]);
    }
}
