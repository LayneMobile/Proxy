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

import com.laynemobile.proxy.elements.ElementAlias;

import java.util.List;

public interface DeclaredTypeAlias extends TypeMirrorAlias {
    /**
     * Returns the element corresponding to this type.
     *
     * @return the element corresponding to this type
     */
    ElementAlias asElement();

    /**
     * Returns the type of the innermost enclosing instance or a {@code NoType} of kind {@code NONE} if there is no
     * enclosing instance.  Only types corresponding to inner classes have an enclosing instance.
     *
     * @return a type mirror for the enclosing type
     *
     * @jls 8.1.3 Inner Classes and Enclosing Instances
     * @jls 15.9.2 Determining Enclosing Instances
     */
    TypeMirrorAlias enclosingType();

    /**
     * Returns the actual type arguments of this type. For a type nested within a parameterized type (such as {@code
     * Outer<String>.Inner<Number>}), only the type arguments of the innermost type are included.
     *
     * @return the actual type arguments of this type, or an empty list if none
     */
    List<? extends TypeMirrorAlias> typeArguments();
}
