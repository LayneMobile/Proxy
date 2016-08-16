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

package com.laynemobile.api;

import com.laynemobile.proxy.annotations.GenerateProxyHandler;

@GenerateProxyHandler
public interface TestInterfaceOverload<T, R> extends TestInterface<T, R> {
    @Override R get(T t);

    R get(String string);

    @Override String fromInteger(int integer);

    String fromInteger(Integer integer);
}
