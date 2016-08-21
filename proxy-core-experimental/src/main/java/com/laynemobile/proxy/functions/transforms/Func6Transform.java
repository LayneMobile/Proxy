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
import com.laynemobile.proxy.functions.Func6;

public class Func6Transform<T1, T2, T3, T4, T5, T6, R>
        extends FunctionTransform<Func6<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6, ? extends R>, R>
        implements Func6<T1, T2, T3, T4, T5, T6, R> {

    public Func6Transform(
            Func6<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6, ? extends R> function) {
        super(function);
    }

    public Func6Transform(
            Func6Transform<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6, ? extends R> function) {
        super(function.function);
    }

    public Func6Transform(final Func0<? extends R> function) {
        super(new Func6<T1, T2, T3, T4, T5, T6, R>() {
            @Override public R call(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6) {
                return function.call();
            }
        });
    }

    public Func6Transform(final R value) {
        super(new Func6<T1, T2, T3, T4, T5, T6, R>() {
            @Override public R call(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6) {
                return value;
            }
        });
    }

    @Override public final R call(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6) {
        return function.call(t1, t2, t3, t4, t5, t6);
    }

    @SuppressWarnings("unchecked")
    @Override public final R call(Object... args) {
        if (args.length != 6) {
            throw new RuntimeException("Func6 expecting 6 arguments.");
        }
        return function.call((T1) args[0], (T2) args[1], (T3) args[2], (T4) args[3], (T5) args[4], (T6) args[5]);
    }
}
