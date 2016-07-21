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
import com.laynemobile.proxy.Util;
import com.laynemobile.proxy.types.AliasTypes;

import java.util.List;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.SimpleAnnotationValueVisitor7;

import static com.laynemobile.proxy.Util.buildList;

final class DefaultAnnotationValueAlias implements AnnotationValueAlias {
    private final Value value;
    private final String toString;

    private DefaultAnnotationValueAlias(AnnotationValue annotationValue) {
        this.value = annotationValue.accept(new Visitor7(), null);
        this.toString = annotationValue.toString();
    }

    static AnnotationValueAlias of(AnnotationValue annotationValue) {
        return new DefaultAnnotationValueAlias(annotationValue);
    }

    static ImmutableList<? extends AnnotationValueAlias> of(List<? extends AnnotationValue> annotationValues) {
        return buildList(annotationValues, new Util.Transformer<AnnotationValueAlias, AnnotationValue>() {
            @Override public AnnotationValueAlias transform(AnnotationValue annotationValue) {
                return of(annotationValue);
            }
        });
    }

    @Override public Value value() {
        return value;
    }

    @Override public String toString() {
        return toString;
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DefaultAnnotationValueAlias)) return false;
        DefaultAnnotationValueAlias that = (DefaultAnnotationValueAlias) o;
        return Objects.equal(value, that.value) &&
                Objects.equal(toString, that.toString);
    }

    @Override public int hashCode() {
        return Objects.hashCode(value, toString);
    }

    private static final class Visitor7 extends SimpleAnnotationValueVisitor7<Value, Void> {
        private Visitor7() {
            super();
        }

        @Override protected Value defaultAction(Object o, Void aVoid) {
            return new Value(Kind.Primitive, o);
        }

        @Override public Value visitAnnotation(AnnotationMirror a, Void aVoid) {
            return new Value(Kind.Annotation, DefaultAnnotationMirrorAlias.of(a));
        }

        @Override public Value visitArray(List<? extends AnnotationValue> vals, Void aVoid) {
            return new Value(Kind.Array, of(vals));
        }

        @Override public Value visitEnumConstant(VariableElement c, Void aVoid) {
            return new Value(Kind.Enum, DefaultVariableElementAlias.of(c));
        }

        @Override public Value visitString(String s, Void aVoid) {
            return new Value(Kind.String, s);
        }

        @Override public Value visitType(TypeMirror t, Void aVoid) {
            return new Value(Kind.Type, AliasTypes.get(t));
        }

        @Override public Value visitUnknown(AnnotationValue av, Void aVoid) {
            return new Value(Kind.Unknown, av.getValue());
        }
    }
}
