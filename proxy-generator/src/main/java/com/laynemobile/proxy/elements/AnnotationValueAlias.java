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

public interface AnnotationValueAlias {
    /**
     * Returns the value.
     *
     * @return the value
     */
    Value value();

    /**
     * Returns a string representation of this value. This is returned in a form suitable for representing this value in
     * the source code of an annotation.
     *
     * @return a string representation of this value
     */
    @Override String toString();

    enum Kind {
        Primitive,
        Array,
        String,
        Annotation,
        Enum,
        Type,
        Unknown
    }

    final class Value {
        private final Kind kind;
        private final Object value;

        public Value(Kind kind, Object value) {
            this.kind = kind;
            this.value = value;
        }

        public Kind kind() {
            return kind;
        }

        public Object value() {
            return value;
        }

        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Value)) return false;
            Value value1 = (Value) o;
            return kind == value1.kind &&
                    Objects.equal(value, value1.value);
        }

        @Override public int hashCode() {
            return Objects.hashCode(kind, value);
        }
    }
}
