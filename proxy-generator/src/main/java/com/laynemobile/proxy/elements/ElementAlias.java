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
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

public interface ElementAlias {
    /**
     * Returns the {@code kind} of this element.
     *
     * @return the kind of this element
     */
    ElementKind kind();

    /**
     * Returns the simple (unqualified) name of this element.  The name of a generic type does not include any reference
     * to its formal type parameters.
     * <p>
     * For example, the simple name of the type element {@code java.util.Set<E>} is {@code "Set"}.
     * <p>
     * If this element represents an unnamed {@linkplain PackageElement#getSimpleName package}, an empty name is
     * returned.
     * <p>
     * If it represents a {@linkplain ExecutableElement#getSimpleName constructor}, the name "{@code <init>}" is
     * returned.  If it represents a {@linkplain ExecutableElement#getSimpleName static initializer}, the name "{@code
     * <clinit>}" is returned.
     * <p>
     * If it represents an {@linkplain TypeElement#getSimpleName anonymous class} or {@linkplain
     * ExecutableElement#getSimpleName instance initializer}, an empty name is returned.
     *
     * @return the simple name of this element
     *
     * @see PackageElement#getSimpleName
     * @see ExecutableElement#getSimpleName
     * @see TypeElement#getSimpleName
     * @see VariableElement#getSimpleName
     */
    String simpleName();

    /**
     * Returns the type defined by this element.
     * <p>
     * <p> A generic element defines a family of types, not just one. If this is a generic element, a
     * <i>prototypical</i> type is returned.  This is the element's invocation on the type variables corresponding to
     * its own formal type parameters. For example, for the generic class element {@code C<N extends Number>}, the
     * parameterized type {@code C<N>} is returned. The {@link Types} utility interface has more general methods for
     * obtaining the full range of types defined by an element.
     *
     * @return the type defined by this element
     *
     * @see Types
     */
    TypeMirrorAlias asType();

    /**
     * Returns the innermost element within which this element is, loosely speaking, enclosed. <ul> <li> If this element
     * is one whose declaration is lexically enclosed immediately within the declaration of another element, that other
     * element is returned.
     * <p>
     * <li> If this is a {@linkplain TypeElement#getEnclosingElement top-level type}, its package is returned.
     * <p>
     * <li> If this is a {@linkplain PackageElement#getEnclosingElement package}, {@code null} is returned.
     * <p>
     * <li> If this is a {@linkplain TypeParameterElement#getEnclosingElement type parameter}, {@linkplain
     * TypeParameterElement#getGenericElement the generic element} of the type parameter is returned.
     * <p>
     * <li> If this is a {@linkplain VariableElement#getEnclosingElement method or constructor parameter}, {@linkplain
     * ExecutableElement the executable element} which declares the parameter is returned.
     * <p>
     * </ul>
     *
     * @return the enclosing element, or {@code null} if there is none
     *
     * @see Elements#getPackageOf
     */
    ElementAlias enclosingElement();

    /**
     * {@inheritDoc}
     * <p>
     * <p> To get inherited annotations as well, use {@link Elements#getAllAnnotationMirrors(Element)
     * getAllAnnotationMirrors}.
     *
     * @since 1.6
     */
    List<? extends AnnotationMirrorAlias> annotationMirrors();

    /**
     * Returns the elements that are, loosely speaking, directly enclosed by this element.
     * <p>
     * A {@linkplain TypeElement#getEnclosedElements class or interface} is considered to enclose the fields, methods,
     * constructors, and member types that it directly declares.
     * <p>
     * A {@linkplain PackageElement#getEnclosedElements package} encloses the top-level classes and interfaces within
     * it, but is not considered to enclose subpackages.
     * <p>
     * Other kinds of elements are not currently considered to enclose any elements; however, that may change as this
     * API or the programming language evolves.
     * <p>
     * <p>Note that elements of certain kinds can be isolated using methods in {@link ElementFilter}.
     *
     * @return the enclosed elements, or an empty list if none
     *
     * @jls 8.8.9 Default Constructor
     * @jls 8.9 Enums
     * @see PackageElement#getEnclosedElements
     * @see TypeElement#getEnclosedElements
     * @see Elements#getAllMembers
     */
    List<? extends ElementAlias> enclosedElements();

    /**
     * Returns the modifiers of this element, excluding annotations. Implicit modifiers, such as the {@code public} and
     * {@code static} modifiers of interface members, are included.
     *
     * @return the modifiers of this element, or an empty set if there are none
     */
    Set<Modifier> modifiers();

    /**
     * Returns {@code true} if the argument represents the same element as {@code this}, or {@code false} otherwise.
     * <p>
     * <p>Note that the identity of an element involves implicit state not directly accessible from the element's
     * methods, including state about the presence of unrelated types.  Element objects created by different
     * implementations of these interfaces should <i>not</i> be expected to be equal even if &quot;the same&quot;
     * element is being modeled; this is analogous to the inequality of {@code Class} objects for the same class file
     * loaded through different class loaders.
     *
     * @param obj
     *         the object to be compared with this element
     *
     * @return {@code true} if the specified object represents the same element as this
     */
    @Override boolean equals(Object obj);

    /**
     * Obeys the general contract of {@link Object#hashCode Object.hashCode}.
     *
     * @see #equals
     */
    @Override int hashCode();
}
