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

public class ConcreteActionDef extends ConcreteFunctionDef<Void> implements ActionDef {
    protected ConcreteActionDef(ActionDef actionInfo) {
        super(actionInfo);
    }

    protected ConcreteActionDef(FunctionDef<Void> functionDef) {
        super(functionDef);
    }

    protected ConcreteActionDef(String name, TypeToken<?>[] paramTypes) {
        super(name, VOID_TYPE, paramTypes);
    }

    static ActionDef create(String name, TypeToken<?>[] paramTypes) {
        return new ConcreteActionDef(name, paramTypes);
    }
}
