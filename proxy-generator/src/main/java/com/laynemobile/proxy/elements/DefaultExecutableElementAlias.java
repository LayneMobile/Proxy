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
import com.google.common.collect.ImmutableList;
import com.laynemobile.proxy.types.AliasTypes;
import com.laynemobile.proxy.types.TypeMirrorAlias;

import javax.lang.model.element.ExecutableElement;

final class DefaultExecutableElementAlias extends DefaultElementAlias implements ExecutableElementAlias {
    private final AnnotationValueAlias defaultValue;
    private final ImmutableList<? extends TypeParameterElementAlias> typeParameters;
    private final TypeMirrorAlias returnType;
    private final ImmutableList<? extends VariableElementAlias> parameters;
    private final ImmutableList<? extends TypeMirrorAlias> thrownTypes;

    private DefaultExecutableElementAlias(ExecutableElement element) {
        super(element);
        this.defaultValue = DefaultAnnotationValueAlias.of(element.getDefaultValue());
        this.typeParameters = DefaultTypeParameterElementAlias.of(element.getTypeParameters());
        this.returnType = AliasTypes.get(element.getReturnType());
        this.parameters = DefaultVariableElementAlias.of(element.getParameters());
        this.thrownTypes = AliasTypes.get(element.getThrownTypes());
    }

    static DefaultExecutableElementAlias of(ExecutableElement element) {
        return new DefaultExecutableElementAlias(element);
    }

    @Override public AnnotationValueAlias defaultValue() {
        return defaultValue;
    }

    @Override public ImmutableList<? extends TypeParameterElementAlias> typeParameters() {
        return typeParameters;
    }

    @Override public TypeMirrorAlias returnType() {
        return returnType;
    }

    @Override public ImmutableList<? extends VariableElementAlias> parameters() {
        return parameters;
    }

    @Override public boolean isVarArgs() {
        return false;
    }

    @Override public ImmutableList<? extends TypeMirrorAlias> thrownTypes() {
        return thrownTypes;
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DefaultExecutableElementAlias)) return false;
        if (!super.equals(o)) return false;
        DefaultExecutableElementAlias that = (DefaultExecutableElementAlias) o;
        return Objects.equal(defaultValue, that.defaultValue) &&
                Objects.equal(typeParameters, that.typeParameters) &&
                Objects.equal(returnType, that.returnType) &&
                Objects.equal(parameters, that.parameters) &&
                Objects.equal(thrownTypes, that.thrownTypes);
    }

    @Override public int hashCode() {
        return Objects.hashCode(super.hashCode(), defaultValue, typeParameters, returnType, parameters, thrownTypes);
    }
}
