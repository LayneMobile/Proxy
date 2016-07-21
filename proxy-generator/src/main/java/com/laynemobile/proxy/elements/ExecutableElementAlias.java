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

import com.laynemobile.proxy.types.TypeMirrorAlias;

import java.util.List;

import javax.lang.model.element.AnnotationValue;
import javax.lang.model.type.NoType;
import javax.lang.model.type.TypeKind;

public interface ExecutableElementAlias extends ElementAlias {
    /**
     * Returns the formal type parameters of this executable in declaration order.
     *
     * @return the formal type parameters, or an empty list if there are none
     */
    List<? extends TypeParameterElementAlias> typeParameters();

    /**
     * Returns the return type of this executable. Returns a {@link NoType} with kind {@link TypeKind#VOID VOID} if this
     * executable is not a method, or is a method that does not return a value.
     *
     * @return the return type of this executable
     */
    TypeMirrorAlias returnType();

    /**
     * Returns the formal parameters of this executable. They are returned in declaration order.
     *
     * @return the formal parameters, or an empty list if there are none
     */
    List<? extends VariableElementAlias> parameters();

    /**
     * Returns {@code true} if this method or constructor accepts a variable number of arguments and returns {@code
     * false} otherwise.
     *
     * @return {@code true} if this method or constructor accepts a variable number of arguments and {@code false}
     * otherwise
     */
    boolean isVarArgs();

    /**
     * Returns the exceptions and other throwables listed in this method or constructor's {@code throws} clause in
     * declaration order.
     *
     * @return the exceptions and other throwables listed in the {@code throws} clause, or an empty list if there are
     * none
     */
    List<? extends TypeMirrorAlias> thrownTypes();

    /**
     * Returns the default value if this executable is an annotation type element.  Returns {@code null} if this method
     * is not an annotation type element, or if it is an annotation type element with no default value.
     *
     * @return the default value, or {@code null} if none
     */
    AnnotationValue defaultValue();

    /**
     * Returns the simple name of a constructor, method, or initializer.  For a constructor, the name {@code "<init>"}
     * is returned, for a static initializer, the name {@code "<clinit>"} is returned, and for an anonymous class or
     * instance initializer, an empty name is returned.
     *
     * @return the simple name of a constructor, method, or initializer
     */
    @Override String simpleName();
}
