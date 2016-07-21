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

import com.laynemobile.proxy.types.DeclaredTypeAlias;

import java.util.Map;

import javax.lang.model.element.AnnotationValue;

public interface AnnotationMirrorAlias {
    /**
     * Returns the type of this annotation.
     *
     * @return the type of this annotation
     */
    DeclaredTypeAlias annotationType();

    /**
     * Returns the values of this annotation's elements. This is returned in the form of a map that associates elements
     * with their corresponding values. Only those elements with values explicitly present in the annotation are
     * included, not those that are implicitly assuming their default values. The order of the map matches the order in
     * which the values appear in the annotation's source.
     * <p>
     * <p>Note that an annotation mirror of a marker annotation type will by definition have an empty map.
     * <p>
     * <p>To fill in default values, use {@link javax.lang.model.util.Elements#getElementValuesWithDefaults
     * getElementValuesWithDefaults}.
     *
     * @return the values of this annotation's elements, or an empty map if there are none
     */
    Map<? extends ExecutableElementAlias, ? extends AnnotationValue> getElementValues();
}
