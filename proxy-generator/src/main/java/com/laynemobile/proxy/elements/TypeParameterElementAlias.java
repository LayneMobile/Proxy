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

import javax.lang.model.element.TypeParameterElement;

public interface TypeParameterElementAlias extends ElementAlias {
    /**
     * Returns the generic class, interface, method, or constructor that is parameterized by this type parameter.
     *
     * @return the generic class, interface, method, or constructor that is parameterized by this type parameter
     */
    ElementAlias genericElement();

    /**
     * Returns the bounds of this type parameter. These are the types given by the {@code extends} clause used to
     * declare this type parameter. If no explicit {@code extends} clause was used, then {@code java.lang.Object} is
     * considered to be the sole bound.
     *
     * @return the bounds of this type parameter, or an empty list if there are none
     */
    List<? extends TypeMirrorAlias> bounds();

    /**
     * Returns the {@linkplain TypeParameterElement#getGenericElement generic element} of this type parameter.
     *
     * @return the generic element of this type parameter
     */
    @Override ElementAlias enclosingElement();
}
