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

import java.util.List;

import javax.lang.model.element.NestingKind;
import javax.lang.model.element.TypeElement;

final class DefaultTypeElementAlias extends DefaultElementAlias implements TypeElementAlias {
    private final String qualifiedName;
    private final NestingKind nestingKind;
    private final TypeMirrorAlias superClass;
    private final ImmutableList<? extends TypeMirrorAlias> interfaces;
    private final ImmutableList<? extends TypeParameterElementAlias> typeParameters;

    private DefaultTypeElementAlias(TypeElement element) {
        super(element);
        this.qualifiedName = element.getQualifiedName().toString();
        this.nestingKind = element.getNestingKind();
        this.superClass = AliasTypes.get(element.getSuperclass());
        this.interfaces = AliasTypes.get(element.getInterfaces());
        this.typeParameters = DefaultTypeParameterElementAlias.of(element.getTypeParameters());
    }

    static TypeElementAlias of(TypeElement element) {
        return new DefaultTypeElementAlias(element);
    }

    static ImmutableList<? extends TypeElementAlias> of(List<? extends TypeElement> typeElements) {
        ImmutableList.Builder<TypeElementAlias> list = ImmutableList.builder();
        for (TypeElement typeElement : typeElements) {
            list.add(of(typeElement));
        }
        return list.build();
    }

    @Override public String qualifiedName() {
        return qualifiedName;
    }

    @Override public NestingKind nestingKind() {
        return nestingKind;
    }

    @Override public TypeMirrorAlias superClass() {
        return superClass;
    }

    @Override public ImmutableList<? extends TypeMirrorAlias> interfaces() {
        return interfaces;
    }

    @Override public ImmutableList<? extends TypeParameterElementAlias> typeParameters() {
        return typeParameters;
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DefaultTypeElementAlias)) return false;
        if (!super.equals(o)) return false;
        DefaultTypeElementAlias that = (DefaultTypeElementAlias) o;
        return Objects.equal(qualifiedName, that.qualifiedName) &&
                nestingKind == that.nestingKind &&
                Objects.equal(superClass, that.superClass) &&
                Objects.equal(interfaces, that.interfaces) &&
                Objects.equal(typeParameters, that.typeParameters);
    }

    @Override public int hashCode() {
        return Objects.hashCode(super.hashCode(), qualifiedName, nestingKind, superClass, interfaces, typeParameters);
    }
}
