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

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.laynemobile.proxy.annotations.GenerateProxyFunction;
import com.laynemobile.proxy.annotations.Generated;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import sourcerer.processor.Env;

public class ProxyFunctionElement extends MethodElement {
    private static final String ABSTRACT_PREFIX = "Abstract";

    private final String name;
    private final TypeElement functionElement;
    private final DeclaredType functionType;
    private final TypeElement abstractProxyFunctionElement;
    private final DeclaredType abstractProxyFunctionType;

    protected ProxyFunctionElement(MethodElement source, Env env) {
        super(source);
        ExecutableElement element = source.element();
        GenerateProxyFunction function = element.getAnnotation(GenerateProxyFunction.class);
        String name = function == null ? "" : function.value();
        if (name.isEmpty()) {
            name = element.getSimpleName().toString();
        }
        env.log("name: %s", name);

        List<TypeMirror> params = source.paramTypes();
        int length = params.size();
        String num;
        if (length > 9) {
            length = 0;
            num = "N";
        } else {
            num = Integer.toString(length);
        }

        TypeMirror returnType = source.returnType();
        String functionClass;
        TypeMirror[] paramTypes;
        env.log("returnType: %s", returnType);
        if (returnType.getKind() == TypeKind.VOID) {
            env.log("void");
            paramTypes = new TypeMirror[length];
            functionClass = "Action";
        } else {
            env.log("not void");
            paramTypes = new TypeMirror[length + 1];
            paramTypes[length] = boxedType(returnType, env);
            functionClass = "Func";
        }

        env.log("params length: %d", length);
        env.log("params: %s", params);
        for (int i = 0; i < length; i++) {
            paramTypes[i] = boxedType(params.get(i), env);
        }
        env.log("paramTypes: %s", Arrays.toString(paramTypes));

        Elements elementUtils = env.elements();
        Types typeUtils = env.types();
        String packageName = "com.laynemobile.proxy.functions.";
        TypeElement functionElement = elementUtils.getTypeElement(packageName + functionClass + num);
        env.log("function element: %s", functionElement);
        DeclaredType functionType = typeUtils.getDeclaredType(functionElement, paramTypes);
        env.log("function type: %s", functionType);

        TypeElement abstractProxyFunctionElement
                = elementUtils.getTypeElement(packageName + "AbstractProxyFunction");
        DeclaredType abstractProxyFunctionType
                = typeUtils.getDeclaredType(abstractProxyFunctionElement, functionType);
        env.log("AbstractProxyFunction type: %s", abstractProxyFunctionType);
        env.log("AbstractProxyFunction type typeArguments: %s", abstractProxyFunctionType.getTypeArguments());

        this.name = name;
        this.functionElement = functionElement;
        this.functionType = functionType;
        this.abstractProxyFunctionElement = abstractProxyFunctionElement;
        this.abstractProxyFunctionType = abstractProxyFunctionType;
    }

    public static ProxyFunctionElement parse(TypeElement typeElement, Element element, Env env) {
        MethodElement source = MethodElement.parse(typeElement, element, env);
        return source == null ? null : from(source, env);
    }

    public static ImmutableList<ProxyFunctionElement> from(List<? extends MethodElement> elements, Env env) {
        ImmutableList.Builder<ProxyFunctionElement> builder = ImmutableList.builder();
        for (MethodElement element : elements) {
            builder.add(from(element, env));
        }
        return builder.build();
    }

    public static ProxyFunctionElement from(MethodElement source, Env env) {
        if (source instanceof ProxyFunctionElement) {
            return (ProxyFunctionElement) source;
        }
        return new ProxyFunctionElement(source, env);
    }

    private static TypeMirror boxedType(TypeMirror typeMirror, Env env) {
        Types typeUtils = env.types();
        if (typeMirror.getKind().isPrimitive()) {
            return typeUtils.boxedClass((PrimitiveType) typeMirror)
                    .asType();
        }
        return typeMirror;
    }

    public void writeTo(Filer filer, Env env) throws IOException {
        TypeElement typeElement = typeElement();
        ProxyElement parent = ProxyElement.cache().get(typeElement);
        if (parent == null) {
            throw new IllegalStateException(typeElement + " parent must be in cache");
        }
        TypeSpec abstractType = newAbstractProxyFunctionTypeSpec();
        String generated = parent.packageName() + ".generated";
        JavaFile abstractTypeFile = JavaFile.builder(generated, abstractType)
                .build();
        abstractTypeFile.writeTo(filer);
//
//        String templates = parent.packageName() + ".templates.temp";
//        String className = abstractType.name.substring(ABSTRACT_PREFIX.length());
//
//        TypeSpec.Builder classBuider = TypeSpec.classBuilder(className)
//                .superclass()
    }

    public TypeSpec newAbstractProxyFunctionTypeSpec() {
        TypeElement typeElement = typeElement();
        ProxyElement parent = ProxyElement.cache().get(typeElement);
        if (parent == null) {
            throw new IllegalStateException(typeElement + " parent must be in cache");
        }
        ExecutableElement element = element();
        String subclassName = typeElement.getSimpleName() + "_" + element.getSimpleName() + "Function";
        String subclassPackage = parent.packageName() + ".templates";
        ClassName subclass = ClassName.get(subclassPackage, subclassName);

        String className = ABSTRACT_PREFIX + subclassName;
        TypeSpec.Builder classBuilder = TypeSpec.classBuilder(className)
                .superclass(TypeName.get(abstractProxyFunctionType))
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .addAnnotation(Generated.class)
                // TODO: add annotation!
//                .addAnnotation(AnnotationSpec.builder(ProxyFunctionImplementation.class)
//                        .addMember("value", "$T.class", subclass)
//                        .build())
                ;
        for (TypeVariable typeVariable : parent.typeVariables()) {
            classBuilder.addTypeVariable(TypeVariableName.get(typeVariable));
        }

        // Constructor
        classBuilder.addMethod(MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PROTECTED)
                .addParameter(TypeName.get(functionType), name)
                .addStatement("super($L)", name)
                .build());

        // handler method
        ClassName NamedMethodHandler = ClassName.get(com.laynemobile.proxy.NamedMethodHandler.class);
        ClassName NamedMethodHandler_Builder = NamedMethodHandler.nestedClass("Builder");
        ClassName FunctionHandlers = ClassName.get(com.laynemobile.proxy.functions.FunctionHandlers.class);
        classBuilder.addMethod(MethodSpec.methodBuilder("handler")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(NamedMethodHandler)
                .addCode(CodeBlock.builder()
                        .add("$[")
                        .add("return new $T()\n", NamedMethodHandler_Builder)
                        .add(".setName($S)\n", element.getSimpleName())
                        .add(".setMethodHandler($T.from(function()))\n", FunctionHandlers)
                        .add(".build()")
                        .add(";\n$]")
                        .build())
                .build());
        return classBuilder.build();
    }

    public JavaFile newAbstractProxyFunctionTypeJavaFile() {
        ProxyElement parent = ProxyElement.cache().get(typeElement());
        if (parent == null) {
            throw new IllegalStateException(typeElement() + " parent must be in cache");
        }
        TypeSpec typeSpec = newAbstractProxyFunctionTypeSpec();
        String packageName = parent.packageName() + ".generated";
        return JavaFile.builder(packageName, typeSpec)
                .build();
    }

    @Override public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("name", name)
                .add("functionElement", functionElement)
                .add("abstractProxyFunctionElement", abstractProxyFunctionElement)
                .add("functionType", functionType)
                .add("abstractProxyFunctionType", abstractProxyFunctionType)
                .toString();
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProxyFunctionElement)) return false;
        if (!super.equals(o)) return false;
        ProxyFunctionElement that = (ProxyFunctionElement) o;
        return Objects.equal(name, that.name) &&
                Objects.equal(functionElement, that.functionElement) &&
                Objects.equal(functionType, that.functionType);
    }

    @Override public int hashCode() {
        return Objects.hashCode(super.hashCode(), name, functionElement, functionType);
    }
}
