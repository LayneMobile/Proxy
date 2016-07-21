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

package com.laynemobile.proxy.elements;

import javax.lang.model.util.Elements;

public interface VariableElementAlias extends ElementAlias {
    /**
     * Returns the value of this variable if this is a {@code final} field initialized to a compile-time constant.
     * Returns {@code null} otherwise.  The value will be of a primitive type or a {@code String}.  If the value is of a
     * primitive type, it is wrapped in the appropriate wrapper class (such as {@link Integer}).
     * <p>
     * <p>Note that not all {@code final} fields will have constant values.  In particular, {@code enum} constants are
     * <em>not</em> considered to be compile-time constants.  To have a constant value, a field's type must be either a
     * primitive type or {@code String}.
     *
     * @return the value of this variable if this is a {@code final} field initialized to a compile-time constant, or
     * {@code null} otherwise
     *
     * @jls 15.28 Constant Expression
     * @jls 4.12.4 final Variables
     * @see Elements#getConstantExpression(Object)
     */
    Object constantValue();

    /**
     * Returns the simple name of this variable element.
     * <p>
     * <p>For method and constructor parameters, the name of each parameter must be distinct from the names of all other
     * parameters of the same executable.  If the original source names are not available, an implementation may
     * synthesize names subject to the distinctness requirement above.
     *
     * @return the simple name of this variable element
     */
    @Override String simpleName();

    /**
     * Returns the enclosing element of this variable.
     * <p>
     * The enclosing element of a method or constructor parameter is the executable declaring the parameter.
     *
     * @return the enclosing element of this variable
     */
    @Override ElementAlias enclosingElement();
}
