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

package com.laynemobile.proxy.model;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.laynemobile.proxy.Util;
import com.laynemobile.proxy.annotations.Generated;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.TypeElement;

import sourcerer.processor.Env;

public abstract class AbstractGeneratedTypeElementStub implements GeneratedTypeElementStub {
    private final String packageName;
    private final String className;
    private final String qualifiedName;

    protected AbstractGeneratedTypeElementStub(String packageName, String className) {
        ClassName typeName = Util.typeName(packageName, className);
        this.packageName = packageName;
        this.className = className;
        this.qualifiedName = Util.qualifiedName(typeName);
    }

    protected abstract TypeSpec build(TypeSpec.Builder classBuilder);

    @Override public final String packageName() {
        return packageName;
    }

    @Override public final String className() {
        return className;
    }

    @Override public final String qualifiedName() {
        return qualifiedName;
    }

    @Override public final TypeSpec newTypeSpec() {
        return build(TypeSpec.classBuilder(className())
                .addAnnotation(Generated.class));
    }

    @Override public JavaFile.Builder newJavaFile() {
        return JavaFile.builder(packageName(), newTypeSpec());
    }

    @Override public TypeElement element(Env env) {
        return env.elements().getTypeElement(qualifiedName);
    }

    @Override public GeneratedTypeElement generatedOutput(Env env) {
        return new AbstractGeneratedTypeElement(this, env) {
            @Override public boolean hasOutput() {
                return false;
            }
        };
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AbstractGeneratedTypeElementStub)) return false;
        AbstractGeneratedTypeElementStub that = (AbstractGeneratedTypeElementStub) o;
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
