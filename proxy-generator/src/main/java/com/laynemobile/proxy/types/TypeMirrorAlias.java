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

import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;

public interface TypeMirrorAlias {
    /**
     * Returns the {@code kind} of this type.
     *
     * @return the kind of this type
     */
    TypeKind kind();

    /**
     * Obeys the general contract of {@link Object#equals Object.equals}. This method does not, however, indicate
     * whether two types represent the same type. Semantic comparisons of type equality should instead use {@link
     * Types#isSameType(TypeMirror, TypeMirror)}. The results of {@code t1.equals(t2)} and {@code Types.isSameType(t1,
     * t2)} may differ.
     *
     * @param obj
     *         the object to be compared with this type
     *
     * @return {@code true} if the specified object is equal to this one
     */
    boolean equals(Object obj);

    /**
     * Obeys the general contract of {@link Object#hashCode Object.hashCode}.
     *
     * @see #equals
     */
    int hashCode();

    /**
     * Returns an informative string representation of this type.  If possible, the string should be of a form suitable
     * for representing this type in source code.  Any names embedded in the result are qualified if possible.
     *
     * @return a string representation of this type
     */
    String toString();
}
