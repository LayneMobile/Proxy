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

class DefaultActionDef<A extends Action> extends DefaultFunctionDef<A, Void>
        implements ActionDef<A> {
    DefaultActionDef(ActionDef<A> actionInfo) {
        super(actionInfo);
    }

    DefaultActionDef(FunctionDef<A, Void> functionDef) {
        super(functionDef);
    }

    DefaultActionDef(String name, A action, TypeToken<?>[] paramTypes) {
        super(name, action, VOID_TYPE, paramTypes);
    }

    static <A extends Action> ActionDef<A> create(String name, A action, TypeToken<?>[] paramTypes) {
        return new DefaultActionDef<>(name, action, paramTypes);
    }
}
