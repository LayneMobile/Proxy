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

public class ProxyFunc1Transform<P, T, R>
        extends ProxyFunctionTransform<P, Func2<? super P, ? super T, ? extends R>, R>
        implements Func2<P, T, R> {

    public ProxyFunc1Transform(Func2<? super P, ? super T, ? extends R> function) {
        super(function);
    }

    public ProxyFunc1Transform(ProxyFunc1Transform<? super P, ? super T, ? extends R> function) {
        super(function.function);
    }

    public ProxyFunc1Transform(final Func0<? extends R> function) {
        super(new Func2<P, T, R>() {
            @Override public R call(P p, T t) {
                return function.call();
            }
        });
    }

    public ProxyFunc1Transform(final R value) {
        super(new Func2<P, T, R>() {
            @Override public R call(P p, T t) {
                return value;
            }
        });
    }

    @Override public final R call(P proxy, T t) {
        return function.call(proxy, t);
    }

    @SuppressWarnings("unchecked")
    @Override public final R call(P proxy, Object... args) {
        if (args.length != 1) {
            throw new RuntimeException("Func1 expecting 1 arguments.");
        }
        return function.call(proxy, (T) args[0]);
    }
}
