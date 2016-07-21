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

import javax.lang.model.element.NestingKind;
import javax.lang.model.type.NoType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.util.Elements;

public interface TypeElementAlias extends ElementAlias {
    /**
     * Returns the fields, methods, constructors, and member types that are directly declared in this class or
     * interface.
     * <p>
     * This includes any (implicit) default constructor and the implicit {@code values} and {@code valueOf} methods of
     * an enum type.
     * <p>
     * <p> Note that as a particular instance of the {@linkplain javax.lang.model.element general accuracy requirements}
     * and the ordering behavior required of this interface, the list of enclosed elements will be returned in the
     * natural order for the originating source of information about the type.  For example, if the information about
     * the type is originating from a source file, the elements will be returned in source code order. (However, in that
     * case the the ordering of synthesized elements, such as a default constructor, is not specified.)
     *
     * @return the enclosed elements in proper order, or an empty list if none
     */
    @Override List<? extends ElementAlias> enclosedElements();

    /**
     * Returns the <i>nesting kind</i> of this type element.
     *
     * @return the nesting kind of this type element
     */
    NestingKind nestingKind();

    /**
     * Returns the fully qualified name of this type element. More precisely, it returns the <i>canonical</i> name. For
     * local and anonymous classes, which do not have canonical names, an empty name is returned.
     * <p>
     * <p>The name of a generic type does not include any reference to its formal type parameters. For example, the
     * fully qualified name of the interface {@code java.util.Set<E>} is "{@code java.util.Set}". Nested types use
     * "{@code .}" as a separator, as in "{@code java.util.Map.Entry}".
     *
     * @return the fully qualified name of this class or interface, or an empty name if none
     *
     * @jls 6.7 Fully Qualified Names and Canonical Names
     * @see Elements#getBinaryName
     */
    String qualifiedName();

    /**
     * Returns the simple name of this type element.
     * <p>
     * For an anonymous class, an empty name is returned.
     *
     * @return the simple name of this class or interface, an empty name for an anonymous class
     */
    @Override String simpleName();

    /**
     * Returns the direct superclass of this type element. If this type element represents an interface or the class
     * {@code java.lang.Object}, then a {@link NoType} with kind {@link TypeKind#NONE NONE} is returned.
     *
     * @return the direct superclass, or a {@code NoType} if there is none
     */
    TypeMirrorAlias superClass();

    /**
     * Returns the interface types directly implemented by this class or extended by this interface.
     *
     * @return the interface types directly implemented by this class or extended by this interface, or an empty list if
     * there are none
     */
    List<? extends TypeMirrorAlias> interfaces();

    /**
     * Returns the formal type parameters of this type element in declaration order.
     *
     * @return the formal type parameters, or an empty list if there are none
     */
    List<? extends TypeParameterElementAlias> typeParameters();

    /**
     * Returns the package of a top-level type and returns the immediately lexically enclosing element for a {@linkplain
     * NestingKind#isNested nested} type.
     *
     * @return the package of a top-level type, the immediately lexically enclosing element for a nested type
     */
    @Override ElementAlias enclosingElement();
}
