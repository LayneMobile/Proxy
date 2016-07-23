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
import com.google.common.collect.ImmutableList;
import com.laynemobile.proxy.Util;
import com.laynemobile.proxy.cache.AbstractCache;
import com.laynemobile.proxy.elements.ElementAlias;

import java.util.List;

import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ErrorType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.NoType;
import javax.lang.model.type.NullType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.TypeVisitor;
import javax.lang.model.type.UnionType;
import javax.lang.model.type.WildcardType;
import javax.lang.model.util.SimpleTypeVisitor7;

public final class AliasTypes {
    private static final Cache CACHE = new Cache();

    private AliasTypes() {}

    public static TypeMirrorAlias get(TypeMirror typeMirror) {
        return getInternal(typeMirror);
    }

    public static DeclaredTypeAlias get(DeclaredType typeMirror) {
        return getInternal(typeMirror);
    }

    public static TypeVariableAlias get(TypeVariable typeMirror) {
        return getInternal(typeMirror);
    }

    public static ImmutableList<? extends TypeMirrorAlias> list(List<? extends TypeMirror> typeMirrors) {
        return buildList(typeMirrors);
    }

    public static ImmutableList<? extends TypeMirrorAlias> parameterTypes(List<? extends TypeMirror> parameterTypes) {
        return buildList(parameterTypes);
    }

    public static ImmutableList<? extends TypeVariableAlias> typeVariables(List<? extends TypeVariable> typeVariables) {
        return buildList(typeVariables);
    }

    @SuppressWarnings("unchecked")
    private static <K extends TypeMirror, V extends TypeMirrorAlias> V getInternal(K k) {
        if (k instanceof TypeMirrorAlias) {
            return (V) k;
        } else if (k == null) {
            return null;
        }
        return (V) CACHE.get(k);
    }

    private static <K extends TypeMirror, V extends TypeMirrorAlias> ImmutableList<? extends V> buildList(
            List<? extends K> in) {
        return Util.buildList(in, new Util.Transformer<V, K>() {
            @Override public V transform(K k) {
                return getInternal(k);
            }
        });
    }

    private static final class Cache extends AbstractCache<TypeMirror, TypeMirrorAlias> {
        private Cache() {}

        @Override protected ForwardingAlias createFutureValue() {
            return new ForwardingAlias();
        }

        @Override protected TypeMirrorAlias create(TypeMirror typeMirror) {
            log("visiting %s - %s", typeMirror.getKind(), typeMirror.getClass());
            return typeMirror.accept(new Visitor7(), null);
        }

//        @Override protected void log(String format, Object... args) {
//            // do nothing
//        }
    }

    private static final class Visitor7 extends SimpleTypeVisitor7<TypeMirrorAlias, Void> {
        private Visitor7() {}

        private Visitor7(TypeMirrorAlias defaultValue) {
            super(defaultValue);
        }

        @Override protected TypeMirrorAlias defaultAction(TypeMirror e, Void aVoid) {
            if (e instanceof TypeMirrorAlias) {
                return (TypeMirrorAlias) e;
            }
            return AbstractTypeMirrorAlias.unknown(e);
        }

        @Override public TypeMirrorAlias visitDeclared(DeclaredType t, Void aVoid) {
            return DefaultDeclaredTypeAlias.of(t);
        }

        @Override public TypeMirrorAlias visitUnion(UnionType t, Void aVoid) {
            return DefaultUnionTypeAlias.of(t);
        }

        @Override public TypeMirrorAlias visitArray(ArrayType t, Void aVoid) {
            return DefaultArrayTypeAlias.of(t);
        }

        @Override public TypeMirrorAlias visitError(ErrorType t, Void aVoid) {
            return DefaultErrorTypeAlias.of(t);
        }

        @Override public TypeMirrorAlias visitExecutable(ExecutableType t, Void aVoid) {
            return DefaultExecutableTypeAlias.of(t);
        }

        @Override public TypeMirrorAlias visitNoType(NoType t, Void aVoid) {
            return DefaultNoTypeAlias.of(t);
        }

        @Override public TypeMirrorAlias visitNull(NullType t, Void aVoid) {
            return DefaultNullTypeAlias.of(t);
        }

        @Override public TypeMirrorAlias visitPrimitive(PrimitiveType t, Void aVoid) {
            return DefaultPrimitiveTypeAlias.of(t);
        }

        @Override public TypeMirrorAlias visitTypeVariable(TypeVariable t, Void aVoid) {
            return DefaultTypeVariableAlias.of(t);
        }

        @Override public TypeMirrorAlias visitWildcard(WildcardType t, Void aVoid) {
            return DefaultWildcardTypeAlias.of(t);
        }
    }

    private static final class ForwardingAlias
            implements TypedTypeMirrorAlias<TypeMirror>,
            ArrayType,
            ErrorType,
            NoType,
            NullType,
            DeclaredType,
            PrimitiveType,
            TypeVariable,
            WildcardType,
            UnionType,
            ExecutableType,
            AbstractCache.FutureValue<TypedTypeMirrorAlias<?>> {
        private TypedTypeMirrorAlias<?> delegate;

        private ForwardingAlias() {}

        @Override public void setDelegate(TypedTypeMirrorAlias<?> delegate) {
            if (this.delegate == null) {
                this.delegate = delegate;
            }
        }

        @Override public TypeMirror actual() {
            return ensure().actual();
        }

        @Override public String toDebugString() {
            return ensure().toDebugString();
        }

        // Basic typemirror

        @Override public <R, P> R accept(TypeVisitor<R, P> v, P p) {
            return ensure().accept(v, p);
        }

        @Override public TypeKind getKind() {
            return ensure().getKind();
        }

        // Array type

        @Override public TypeMirror getComponentType() {
            return arrayType().getComponentType();
        }

        // declared type

        @Override public List<? extends TypeMirrorAlias> getTypeArguments() {
            return declaredType().getTypeArguments();
        }

        @Override public TypeMirrorAlias getEnclosingType() {
            return declaredType().getEnclosingType();
        }

        @Override public ElementAlias asElement() {
            return declaredType().asElement();
        }

        // wildcard

        @Override public TypeMirrorAlias getExtendsBound() {
            return wildcardType().getExtendsBound();
        }

        @Override public TypeMirrorAlias getSuperBound() {
            return wildcardType().getSuperBound();
        }

        // type variable

        @Override public TypeMirrorAlias getUpperBound() {
            return typeVariable().getUpperBound();
        }

        @Override public TypeMirrorAlias getLowerBound() {
            return typeVariable().getLowerBound();
        }

        // union type

        @Override public List<? extends TypeMirrorAlias> getAlternatives() {
            return unionType().getAlternatives();
        }

        // executable

        @Override public List<? extends TypeMirrorAlias> getParameterTypes() {
            return executableType().getParameterTypes();
        }

        @Override public TypeMirrorAlias getReturnType() {
            return executableType().getReturnType();
        }

        @Override public List<? extends TypeMirrorAlias> getThrownTypes() {
            return executableType().getThrownTypes();
        }

        @Override public List<? extends TypeVariableAlias> getTypeVariables() {
            return executableType().getTypeVariables();
        }

        // equals & hash

        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof TypeMirrorAlias)) return false;
            TypeMirrorAlias od = o instanceof ForwardingAlias
                    ? ((ForwardingAlias) o).delegate
                    : (TypeMirrorAlias) o;
            return Objects.equal(delegate, od);
        }

        @Override public int hashCode() {
            return Objects.hashCode(delegate);
        }

        @Override public String toString() {
            return delegate.toString();
        }

        private TypedTypeMirrorAlias<?> ensure() {
            TypedTypeMirrorAlias<?> d = delegate;
            if (d == null) {
                throw new NullPointerException("delegate is null");
            }
            return d;
        }

        @SuppressWarnings("unchecked")
        private <T extends TypeMirrorAlias> T cast(String message) {
            try {
                return (T) ensure();
            } catch (ClassCastException e) {
                throw new UnsupportedOperationException(message, e);
            }
        }

        private ArrayTypeAlias arrayType() {
            return cast("not an ArrayTypeAlias");
        }

        private DeclaredTypeAlias declaredType() {
            return cast("not a DeclaredTypeAlias");
        }

        private WildcardTypeAlias wildcardType() {
            return cast("not a WildcardTypeAlias");
        }

        private TypeVariableAlias typeVariable() {
            return cast("not a TypeVariableAlias");
        }

        private UnionTypeAlias unionType() {
            return cast("not an UnionTypeAlias");
        }

        private ExecutableTypeAlias executableType() {
            return cast("not an ExecutableTypeAlias");
        }
    }
}
