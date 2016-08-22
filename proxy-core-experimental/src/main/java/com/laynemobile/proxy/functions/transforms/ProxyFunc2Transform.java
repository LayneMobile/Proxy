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
import com.laynemobile.proxy.functions.Func3;

public class ProxyFunc2Transform<P, T1, T2, R>
        extends ProxyFunctionTransform<P, Func3<? super P, ? super T1, ? super T2, ? extends R>, R>
        implements Func3<P, T1, T2, R> {

    public ProxyFunc2Transform(Func3<? super P, ? super T1, ? super T2, ? extends R> function) {
        super(function);
    }

    public ProxyFunc2Transform(ProxyFunc2Transform<? super P, ? super T1, ? super T2, ? extends R> function) {
        super(function.function);
    }

    public ProxyFunc2Transform(final Func0<? extends R> function) {
        super(new Func3<P, T1, T2, R>() {
            @Override public R call(P p, T1 t1, T2 t2) {
                return function.call();
            }
        });
    }

    public ProxyFunc2Transform(final R value) {
        super(new Func3<P, T1, T2, R>() {
            @Override public R call(P p, T1 t1, T2 t2) {
                return value;
            }
        });
    }

    @Override public final R call(P proxy, T1 t1, T2 t2) {
        return function.call(proxy, t1, t2);
    }

    @SuppressWarnings("unchecked")
    @Override public final R call(P proxy, Object... args) {
        if (args.length != 2) {
            throw new RuntimeException("Func2 expecting 2 arguments.");
        }
        return function.call(proxy, (T1) args[0], (T2) args[1]);
    }
}
