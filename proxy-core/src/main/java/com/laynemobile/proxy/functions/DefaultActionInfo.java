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

package com.laynemobile.proxy.functions;

import com.laynemobile.proxy.TypeToken;

class DefaultActionInfo<A extends Action> extends DefaultFunctionInfo<A, Void>
        implements ActionInfo<A> {
    DefaultActionInfo(ActionInfo<A> actionInfo) {
        super(actionInfo);
    }

    DefaultActionInfo(FunctionInfo<A, Void> functionInfo) {
        super(functionInfo);
    }

    DefaultActionInfo(String name, A action, TypeToken<?>[] paramTypes) {
        super(name, action, VOID_TYPE, paramTypes);
    }

    static <A extends Action> ActionInfo<A> create(String name, A action, TypeToken<?>[] paramTypes) {
        return new DefaultActionInfo<>(name, action, paramTypes);
    }
}
