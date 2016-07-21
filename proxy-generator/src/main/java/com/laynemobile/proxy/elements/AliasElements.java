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

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.laynemobile.proxy.cache.AbstractCache;
import com.laynemobile.proxy.types.TypeMirrorAlias;

import java.util.List;
import java.util.Set;

import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.SimpleElementVisitor7;

public final class AliasElements {
//    private static final Cache CACHE = new Cache();

    private AliasElements() {}

    public static ElementAlias get(Element element) {
        return element.accept(new Visitor7(), null);
    }

    public static TypeElementAlias get(TypeElement typeElement) {
        return DefaultTypeElementAlias.of(typeElement);
    }

//    private static final class Cache extends AbstractCache<Element, ElementAlias> {
//        private Cache() {}
//
//        @Override protected ForwardingAlias createFutureValue() {
//            return new ForwardingAlias();
//        }
//
//        @Override protected ElementAlias create(Element element) {
//            return element.accept(new Visitor7(), null);
//        }
//    }

    private static final class Visitor7 extends SimpleElementVisitor7<ElementAlias, Void> {
        private Visitor7() {}

        private Visitor7(ElementAlias defaultValue) {
            super(defaultValue);
        }

        @Override protected ElementAlias defaultAction(Element e, Void aVoid) {
            return DefaultElementAlias.of(e);
        }

        @Override public ElementAlias visitType(TypeElement e, Void aVoid) {
            return DefaultTypeElementAlias.of(e);
        }

        @Override public ElementAlias visitTypeParameter(TypeParameterElement e, Void aVoid) {
            return DefaultTypeParameterElementAlias.of(e);
        }

        @Override public ElementAlias visitVariable(VariableElement e, Void aVoid) {
            return super.visitVariable(e, aVoid);
        }

        @Override public ElementAlias visitExecutable(ExecutableElement e, Void aVoid) {
            return super.visitExecutable(e, aVoid);
        }
    }

    private static final class ForwardingAlias
            implements ElementAlias,
            TypeElementAlias,
            TypeParameterElementAlias,
            ExecutableElementAlias,
            VariableElementAlias,
            AbstractCache.FutureValue<ElementAlias> {
        private ElementAlias delegate;

        private ForwardingAlias() {}

        @Override public void setDelegate(ElementAlias delegate) {
            if (this.delegate != null) {
                this.delegate = delegate;
            }
        }

        // basic element

        @Override public ElementKind kind() {
            return ensure().kind();
        }

        @Override public String simpleName() {
            return ensure().simpleName();
        }

        @Override public TypeMirrorAlias asType() {
            return ensure().asType();
        }

        @Override public ElementAlias enclosingElement() {
            return ensure().enclosingElement();
        }

        @Override public List<? extends AnnotationMirrorAlias> annotationMirrors() {
            return ensure().annotationMirrors();
        }

        @Override public List<? extends ElementAlias> enclosedElements() {
            return ensure().enclosedElements();
        }

        @Override public Set<Modifier> modifiers() {
            return ensure().modifiers();
        }

        // type element

        @Override public List<? extends TypeMirrorAlias> interfaces() {
            return typeElement().interfaces();
        }

        @Override public NestingKind nestingKind() {
            return typeElement().nestingKind();
        }

        @Override public String qualifiedName() {
            return typeElement().qualifiedName();
        }

        @Override public TypeMirrorAlias superClass() {
            return typeElement().superClass();
        }

        @Override public List<? extends TypeParameterElementAlias> typeParameters() {
            return typeElement().typeParameters();
        }

        // type parameter element

        @Override public List<? extends TypeMirrorAlias> bounds() {
            return typeParameterElement().bounds();
        }

        @Override public ElementAlias genericElement() {
            return typeParameterElement().genericElement();
        }

        // executable element

        @Override public AnnotationValue defaultValue() {
            return executableElement().defaultValue();
        }

        @Override public TypeMirrorAlias returnType() {
            return executableElement().returnType();
        }

        @Override public List<? extends VariableElementAlias> parameters() {
            return executableElement().parameters();
        }

        @Override public boolean isVarArgs() {
            return executableElement().isVarArgs();
        }

        @Override public List<? extends TypeMirrorAlias> thrownTypes() {
            return executableElement().thrownTypes();
        }

        // variable element

        @Override public Object constantValue() {
            return variableElement().constantValue();
        }

        // equals & hash

        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ElementAlias)) return false;
            ElementAlias od = o instanceof ForwardingAlias
                    ? ((ForwardingAlias) o).delegate
                    : (ElementAlias) o;
            return Objects.equal(delegate, od);
        }

        @Override public int hashCode() {
            return Objects.hashCode(delegate);
        }

        @Override public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("delegate", delegate)
                    .toString();
        }

        private ElementAlias ensure() {
            ElementAlias d = delegate;
            if (d == null) {
                throw new NullPointerException("delegate is null");
            }
            return d;
        }

        @SuppressWarnings("unchecked")
        private <T extends ElementAlias> T cast(String message) {
            try {
                return (T) ensure();
            } catch (ClassCastException e) {
                throw new UnsupportedOperationException(message, e);
            }
        }

        private TypeElementAlias typeElement() {
            return cast("not a TypeElementAlias");
        }

        private TypeParameterElementAlias typeParameterElement() {
            return cast("not a TypeParameterElementAlias");
        }

        private ExecutableElementAlias executableElement() {
            return cast("not an ExecutableElementAlias");
        }

        private VariableElementAlias variableElement() {
            return cast("not a VariableElementAlias");
        }
    }
}
