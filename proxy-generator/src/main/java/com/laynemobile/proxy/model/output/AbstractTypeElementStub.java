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

package com.laynemobile.proxy.model.output;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.laynemobile.proxy.Util;
import com.squareup.javapoet.ClassName;

import javax.lang.model.element.TypeElement;

import sourcerer.processor.Env;

public abstract class AbstractTypeElementStub implements TypeElementStub {
    private final String packageName;
    private final String className;
    private final String qualifiedName;

    protected AbstractTypeElementStub(String packageName, String className) {
        ClassName typeName = Util.typeName(packageName, className);
        this.packageName = packageName;
        this.className = className;
        this.qualifiedName = Util.qualifiedName(typeName);
    }

    @Override public final String packageName() {
        return packageName;
    }

    @Override public final String className() {
        return className;
    }

    @Override public final String qualifiedName() {
        return qualifiedName;
    }

    @Override public TypeElement element(Env env) {
        return env.elements().getTypeElement(qualifiedName);
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AbstractTypeElementStub)) return false;
        AbstractTypeElementStub that = (AbstractTypeElementStub) o;
        return Objects.equal(qualifiedName, that.qualifiedName);
    }

    @Override public int hashCode() {
        return Objects.hashCode(qualifiedName);
    }

    @Override public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("qualifiedName", qualifiedName)
                .toString();
    }
}
