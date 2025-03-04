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

package com.laynemobile.proxy.types;

import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVisitor;
import java.lang.annotation.Annotation;

public interface TypeMirrorAlias extends TypeMirror {
    TypeMirror actual();

    /** {@inheritDoc} */
    @Override TypeKind getKind();

    /** {@inheritDoc} */
    @Override <R, P> R accept(TypeVisitor<R, P> v, P p);

    @Override <A extends Annotation> A[] getAnnotationsByType(Class<A> type);
}
