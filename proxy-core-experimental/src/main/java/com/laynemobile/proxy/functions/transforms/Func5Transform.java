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
import com.laynemobile.proxy.functions.Func5;

public class Func5Transform<T1, T2, T3, T4, T5, R>
        extends FunctionTransform<Func5<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? extends R>, R>
        implements Func5<T1, T2, T3, T4, T5, R> {

    public Func5Transform(Func5<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? extends R> function) {
        super(function);
    }

    public Func5Transform(final Func0<? extends R> function) {
        super(new Func5<T1, T2, T3, T4, T5, R>() {
            @Override public R call(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5) {
                return function.call();
            }
        });
    }

    public Func5Transform(final R value) {
        super(new Func5<T1, T2, T3, T4, T5, R>() {
            @Override public R call(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5) {
                return value;
            }
        });
    }

    @Override public final R call(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5) {
        return function.call(t1, t2, t3, t4, t5);
    }

    @SuppressWarnings("unchecked")
    @Override public final R call(Object... args) {
        if (args.length != 5) {
            throw new RuntimeException("Func5 expecting 5 arguments.");
        }
        return function.call((T1) args[0], (T2) args[1], (T3) args[2], (T4) args[3], (T5) args[4]);
    }
}
