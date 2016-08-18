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
import com.laynemobile.proxy.functions.FunctionTransform;

import static com.laynemobile.proxy.functions.Functions.toFunc0;

public class Func0Transform<R>
        extends FunctionTransform<Func0<? extends R>>
        implements Func0<R> {

    public Func0Transform(Func0<? extends R> function) {
        super(function);
    }

    public Func0Transform(final R value) {
        super(toFunc0(value));
    }

    @Override public final R call() {
        return function.call();
    }
}
