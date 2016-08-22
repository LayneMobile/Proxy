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
import com.laynemobile.proxy.functions.Func4;

public class ProxyFunc3Transform<P, T1, T2, T3, R>
        extends ProxyFunctionTransform<P, Func4<? super P, ? super T1, ? super T2, ? super T3, ? extends R>, R>
        implements Func4<P, T1, T2, T3, R> {

    public ProxyFunc3Transform(Func4<? super P, ? super T1, ? super T2, ? super T3, ? extends R> function) {
        super(function);
    }

    public ProxyFunc3Transform(
            ProxyFunc3Transform<? super P, ? super T1, ? super T2, ? super T3, ? extends R> function) {
        super(function.function);
    }

    public ProxyFunc3Transform(final Func0<? extends R> function) {
        super(new Func4<P, T1, T2, T3, R>() {
            @Override public R call(P p, T1 t1, T2 t2, T3 t3) {
                return function.call();
            }
        });
    }

    public ProxyFunc3Transform(final R value) {
        super(new Func4<P, T1, T2, T3, R>() {
            @Override public R call(P p, T1 t1, T2 t2, T3 t3) {
                return value;
            }
        });
    }

    @Override public final R call(P proxy, T1 t1, T2 t2, T3 t3) {
        return function.call(proxy, t1, t2, t3);
    }

    @SuppressWarnings("unchecked")
    @Override public final R call(P proxy, Object... args) {
        if (args.length != 3) {
            throw new RuntimeException("Func3 expecting 3 arguments.");
        }
        return function.call(proxy, (T1) args[0], (T2) args[1], (T3) args[2]);
    }
}
