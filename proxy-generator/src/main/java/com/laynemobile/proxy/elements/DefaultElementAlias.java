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
import com.google.common.collect.ImmutableSet;
import com.laynemobile.proxy.types.AliasTypes;
import com.laynemobile.proxy.types.TypeMirrorAlias;

import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;

class DefaultElementAlias implements ElementAlias {
    private final ElementKind kind;
    private final String simpleName;
    private final TypeMirrorAlias type;
    private final ElementAlias enclosingElement;
    private final ImmutableList<? extends AnnotationMirrorAlias> annotationMirrors;
    private final ImmutableList<? extends ElementAlias> enclosedElements;
    private final ImmutableSet<Modifier> modifiers;

    DefaultElementAlias(Element element) {
        this.kind = element.getKind();
        this.simpleName = element.getSimpleName().toString();
        this.type = AliasTypes.get(element.asType());
        this.enclosingElement = of(element);
        this.annotationMirrors = DefaultAnnotationMirrorAlias.of(element.getAnnotationMirrors());
        this.enclosedElements = list(element.getEnclosedElements());
        this.modifiers = ImmutableSet.copyOf(element.getModifiers());
    }

    static ElementAlias of(Element element) {
        return new DefaultElementAlias(element);
    }

    static ImmutableList<? extends ElementAlias> list(List<? extends Element> elements) {
        ImmutableList.Builder<ElementAlias> list = ImmutableList.builder();
        for (Element element : elements) {
            list.add(of(element));
        }
        return list.build();
    }

    @Override public final ElementKind kind() {
        return kind;
    }

    @Override public final List<? extends AnnotationMirrorAlias> annotationMirrors() {
        return annotationMirrors;
    }

    @Override public final String simpleName() {
        return simpleName;
    }

    @Override public final TypeMirrorAlias asType() {
        return type;
    }

    @Override public final ElementAlias enclosingElement() {
        return enclosingElement;
    }

    @Override public final ImmutableList<? extends ElementAlias> enclosedElements() {
        return enclosedElements;
    }

    @Override public final ImmutableSet<Modifier> modifiers() {
        return modifiers;
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DefaultElementAlias)) return false;
        DefaultElementAlias that = (DefaultElementAlias) o;
        return kind == that.kind &&
                Objects.equal(simpleName, that.simpleName) &&
                Objects.equal(type, that.type) &&
                Objects.equal(enclosingElement, that.enclosingElement) &&
                Objects.equal(annotationMirrors, that.annotationMirrors) &&
                Objects.equal(enclosedElements, that.enclosedElements) &&
                Objects.equal(modifiers, that.modifiers);
    }

    @Override public int hashCode() {
        return Objects.hashCode(kind, simpleName, type, enclosingElement, annotationMirrors, enclosedElements,
                modifiers);
    }
}
