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

import com.google.common.collect.ImmutableList;

import java.util.List;

import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ErrorType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.NoType;
import javax.lang.model.type.NullType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.UnionType;
import javax.lang.model.type.WildcardType;
import javax.lang.model.util.SimpleTypeVisitor7;

public final class AliasTypes {
    private AliasTypes() {}

    public static TypeMirrorAlias get(TypeMirror typeMirror) {
        return typeMirror.accept(new Visitor7(), null);
    }

    public static DeclaredTypeAlias get(DeclaredType declaredType) {
        return DefaultDeclaredTypeAlias.of(declaredType);
    }

    public static ImmutableList<? extends TypeMirrorAlias> get(List<? extends TypeMirror> typeMirrors) {
        ImmutableList.Builder<TypeMirrorAlias> list = ImmutableList.builder();
        for (TypeMirror typeMirror : typeMirrors) {
            list.add(get(typeMirror));
        }
        return list.build();
    }

    private static final class Visitor7 extends SimpleTypeVisitor7<TypeMirrorAlias, Void> {
        private Visitor7() {
        }

        private Visitor7(TypeMirrorAlias defaultValue) {
            super(defaultValue);
        }

        @Override protected TypeMirrorAlias defaultAction(TypeMirror e, Void aVoid) {
            return DefaultTypeMirrorAlias.of(e);
        }

        @Override public TypeMirrorAlias visitDeclared(DeclaredType t, Void aVoid) {
            return DefaultDeclaredTypeAlias.of(t);
        }

        @Override public TypeMirrorAlias visitUnion(UnionType t, Void aVoid) {
            return super.visitUnion(t, aVoid);
        }

        @Override public TypeMirrorAlias visitArray(ArrayType t, Void aVoid) {
            return super.visitArray(t, aVoid);
        }

        @Override public TypeMirrorAlias visitError(ErrorType t, Void aVoid) {
            return super.visitError(t, aVoid);
        }

        @Override public TypeMirrorAlias visitExecutable(ExecutableType t, Void aVoid) {
            return super.visitExecutable(t, aVoid);
        }

        @Override public TypeMirrorAlias visitNoType(NoType t, Void aVoid) {
            return super.visitNoType(t, aVoid);
        }

        @Override public TypeMirrorAlias visitNull(NullType t, Void aVoid) {
            return super.visitNull(t, aVoid);
        }

        @Override public TypeMirrorAlias visitPrimitive(PrimitiveType t, Void aVoid) {
            return super.visitPrimitive(t, aVoid);
        }

        @Override public TypeMirrorAlias visitTypeVariable(TypeVariable t, Void aVoid) {
            return super.visitTypeVariable(t, aVoid);
        }

        @Override public TypeMirrorAlias visitWildcard(WildcardType t, Void aVoid) {
            return super.visitWildcard(t, aVoid);
        }

        @Override public TypeMirrorAlias visitUnknown(TypeMirror t, Void aVoid) {
            return super.visitUnknown(t, aVoid);
        }
    }
}
