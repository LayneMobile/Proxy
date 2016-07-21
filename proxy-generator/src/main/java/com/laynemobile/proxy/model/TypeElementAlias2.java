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

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.type.TypeMirror;

public interface TypeElementAlias2 extends Element {
    @Override <R, P> R accept(ElementVisitor<R, P> v, P p);

    @Override ElementKind getKind();

    @Override Name getSimpleName();

    @Override TypeMirror asType();

    @Override Element getEnclosingElement();

    @Override List<? extends AnnotationMirror> getAnnotationMirrors();

    @Override List<? extends Element> getEnclosedElements();

    @Override Set<Modifier> getModifiers();

    @Override <A extends Annotation> A getAnnotation(Class<A> annotationType);

    @Override boolean equals(Object obj);

    @Override int hashCode();
}
