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

import com.google.common.collect.ImmutableList;
import com.laynemobile.proxy.types.AliasTypes;
import com.laynemobile.proxy.types.DeclaredTypeAlias;

import java.util.List;
import java.util.Map;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;

final class DefaultAnnotationMirrorAlias implements AnnotationMirrorAlias {
    private final DeclaredTypeAlias annotationType;

    private DefaultAnnotationMirrorAlias(AnnotationMirror annotationMirror) {
        this.annotationType = AliasTypes.get(annotationMirror.getAnnotationType());
    }

    static AnnotationMirrorAlias of(AnnotationMirror annotationMirror) {
        return new DefaultAnnotationMirrorAlias(annotationMirror);
    }

    static ImmutableList<? extends AnnotationMirrorAlias> of(List<? extends AnnotationMirror> annotationMirrors) {
        ImmutableList.Builder<AnnotationMirrorAlias> list = ImmutableList.builder();
        for (AnnotationMirror annotationMirror : annotationMirrors) {
            list.add(of(annotationMirror));
        }
        return list.build();
    }

    @Override public DeclaredTypeAlias annotationType() {
        return annotationType;
    }

    @Override public Map<? extends ExecutableElementAlias, ? extends AnnotationValue> getElementValues() {
        // TODO:
        return null;
    }
}
