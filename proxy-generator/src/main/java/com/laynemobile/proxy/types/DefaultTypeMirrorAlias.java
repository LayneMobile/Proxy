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

import com.google.common.base.Objects;

import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

class DefaultTypeMirrorAlias implements TypeMirrorAlias {
    private final TypeKind kind;
    private final String toString;

    DefaultTypeMirrorAlias(TypeMirror typeMirror) {
        this.kind = typeMirror.getKind();
        this.toString = typeMirror.toString();
    }

    static DefaultTypeMirrorAlias of(TypeMirror typeMirror) {
        return new DefaultTypeMirrorAlias(typeMirror);
    }

    @Override public TypeKind kind() {
        return kind;
    }

    @Override public final String toString() {
        return toString;
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DefaultTypeMirrorAlias)) return false;
        DefaultTypeMirrorAlias that = (DefaultTypeMirrorAlias) o;
        return kind == that.kind &&
                Objects.equal(toString, that.toString);
    }

    @Override public int hashCode() {
        return Objects.hashCode(kind, toString);
    }
}
