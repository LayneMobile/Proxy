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

import java.util.List;

import javax.lang.model.element.NestingKind;

public interface PackageElementAlias extends ElementAlias {
    /**
     * Returns the fully qualified name of this package. This is also known as the package's <i>canonical</i> name.
     *
     * @return the fully qualified name of this package, or an empty name if this is an unnamed package
     *
     * @jls 6.7 Fully Qualified Names and Canonical Names
     */
    String qualifiedName();

    /**
     * Returns the simple name of this package.  For an unnamed package, an empty name is returned.
     *
     * @return the simple name of this package or an empty name if this is an unnamed package
     */
    @Override String simpleName();

    /**
     * Returns the {@linkplain NestingKind#TOP_LEVEL top-level} classes and interfaces within this package.  Note that
     * subpackages are <em>not</em> considered to be enclosed by a package.
     *
     * @return the top-level classes and interfaces within this package
     */
    @Override List<? extends ElementAlias> enclosedElements();

    /**
     * Returns {@code true} is this is an unnamed package and {@code false} otherwise.
     *
     * @return {@code true} is this is an unnamed package and {@code false} otherwise
     *
     * @jls 7.4.2 Unnamed Packages
     */
    boolean isUnnamed();

    /**
     * Returns {@code null} since a package is not enclosed by another element.
     *
     * @return {@code null}
     */
    @Override ElementAlias enclosingElement();
}
