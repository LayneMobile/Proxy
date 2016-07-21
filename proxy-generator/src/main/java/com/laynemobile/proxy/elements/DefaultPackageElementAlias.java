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

import com.google.common.base.Objects;

import javax.lang.model.element.PackageElement;

final class DefaultPackageElementAlias extends DefaultElementAlias implements PackageElementAlias {
    private final boolean unnamed;
    private final String qualifiedName;

    private DefaultPackageElementAlias(PackageElement element) {
        super(element);
        this.unnamed = element.isUnnamed();
        this.qualifiedName = element.getQualifiedName().toString();
    }

    static PackageElementAlias of(PackageElement element) {
        return new DefaultPackageElementAlias(element);
    }

    @Override public boolean isUnnamed() {
        return unnamed;
    }

    @Override public String qualifiedName() {
        return qualifiedName;
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DefaultPackageElementAlias)) return false;
        if (!super.equals(o)) return false;
        DefaultPackageElementAlias that = (DefaultPackageElementAlias) o;
        return unnamed == that.unnamed &&
                Objects.equal(qualifiedName, that.qualifiedName);
    }

    @Override public int hashCode() {
        return Objects.hashCode(super.hashCode(), unnamed, qualifiedName);
    }
}
