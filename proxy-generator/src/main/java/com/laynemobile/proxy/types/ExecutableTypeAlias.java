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

package com.laynemobile.proxy.types;

import java.util.List;

import javax.lang.model.type.ExecutableType;

public interface ExecutableTypeAlias extends TypedTypeMirrorAlias<ExecutableType>, ExecutableType {
    @Override List<? extends TypeMirrorAlias> getParameterTypes();

    @Override TypeMirrorAlias getReturnType();

    @Override List<? extends TypeMirrorAlias> getThrownTypes();

    @Override List<? extends TypeVariableAlias> getTypeVariables();

    @Override TypeMirrorAlias getReceiverType();
}
